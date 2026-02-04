package org.maverick.devtools.worker.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.maverick.devtools.core.config.CamundaProperties;
import org.maverick.devtools.worker.annotation.ZeebeWorker;
import org.maverick.devtools.worker.metrics.WorkerMetrics;
import org.maverick.devtools.worker.exception.ZeebeBpmnError;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ZeebeWorkerPostProcessor implements BeanPostProcessor, SmartLifecycle, DisposableBean {

    private final ZeebeClient zeebeClient;
    private final CamundaProperties properties;
    private final WorkerMetrics metrics;
    private final ObjectMapper objectMapper; // Injected
    private final List<JobWorker> openedWorkers = new ArrayList<>();
    private boolean running = false;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            ZeebeWorker annotation = method.getAnnotation(ZeebeWorker.class);
            if (annotation != null) {
                log.info("Registering Zeebe worker for method: {}#{}", bean.getClass().getSimpleName(),
                        method.getName());
                configureWorker(annotation, method, bean);
            }
        });
        return bean;
    }

    private void configureWorker(ZeebeWorker annotation, Method method, Object bean) {
        JobWorkerBuilderStep1.JobWorkerBuilderStep3 builder = zeebeClient.newWorker()
                .jobType(annotation.type())
                .handler(new ReflectiveJobHandler(method, bean, metrics, annotation.type(), objectMapper));

        if (!annotation.name().isEmpty()) {
            builder.name(annotation.name());
        }

        if (annotation.timeout() > 0) {
            builder.timeout(Duration.ofMillis(annotation.timeout()));
        }

        if (annotation.maxJobsActive() > 0) {
            builder.maxJobsActive(annotation.maxJobsActive());
        } else if (properties.getWorker().getThreads() != null) {
            // Fallback for deprecated threads property
            builder.maxJobsActive(properties.getWorker().getThreads());
        } else if (properties.getWorker().getMaxJobsActive() != null) {
            builder.maxJobsActive(properties.getWorker().getMaxJobsActive());
        }

        if (properties.getWorker().getPollInterval() != null) {
            builder.pollInterval(Duration.ofMillis(properties.getWorker().getPollInterval()));
        }

        if (properties.getWorker().getRequestTimeout() != null) {
            builder.requestTimeout(Duration.ofMillis(properties.getWorker().getRequestTimeout()));
        }

        if (properties.getWorker().isStreamEnabled()) {
            builder.streamEnabled(true);
        }

        if (annotation.fetchAllVariables()) {
            builder.fetchVariables();
        } else if (annotation.fetchVariables().length > 0) {
            builder.fetchVariables(annotation.fetchVariables());
        }

        JobWorker worker = builder.open();
        openedWorkers.add(worker);
        log.info("Opened worker for type: {}", annotation.type());
    }

    @Override
    public void start() {
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        openedWorkers.forEach(JobWorker::close);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void destroy() {
        stop();
    }

    // Internal Handler Class
    @RequiredArgsConstructor
    private static class ReflectiveJobHandler implements JobHandler {
        private final Method method;
        private final Object bean;
        private final WorkerMetrics metrics;
        private final String jobType;
        private final ObjectMapper objectMapper;

        @Override
        public void handle(JobClient client, ActivatedJob job) throws Exception {
            var sample = metrics.startTimer();
            String status = "SUCCESS";
            try (org.slf4j.MDC.MDCCloseable ignored = org.slf4j.MDC.putCloseable("jobKey",
                    String.valueOf(job.getKey()))) {
                org.slf4j.MDC.put("processInstanceKey", String.valueOf(job.getProcessInstanceKey()));

                Object[] args = resolveArgs(method, client, job);
                Object result = method.invoke(bean, args);

                client.newCompleteCommand(job.getKey())
                        .variables(result) // ObjectMapper can handle POJO serialization automatically here usually, or
                                           // check below
                        .send()
                        .join();
                metrics.recordJobSuccess(jobType);
            } catch (InvocationTargetException e) {
                Throwable target = e.getTargetException();
                if (target instanceof ZeebeBpmnError) {
                    status = "BPMN_ERROR";
                    ZeebeBpmnError bpmnError = (ZeebeBpmnError) target;
                    log.warn("Worker threw BPMN error: {} - {}", bpmnError.getErrorCode(), bpmnError.getErrorMessage());
                    client.newThrowErrorCommand(job.getKey())
                            .errorCode(bpmnError.getErrorCode())
                            .errorMessage(bpmnError.getErrorMessage())
                            .send()
                            .join();
                    // Do NOT record failure for BPMN error, it's an expected business outcome
                } else {
                    handleException(client, job, (Exception) target);
                }
            } catch (Exception e) {
                handleException(client, job, e);
            } finally {
                metrics.recordJobDuration(sample, jobType, status);
                org.slf4j.MDC.remove("processInstanceKey");
            }
        }

        private void handleException(JobClient client, ActivatedJob job, Exception e) {
            metrics.recordJobFailure(jobType, e.getClass().getSimpleName());
            log.error("Worker failed for job {}", job.getKey(), e);
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }

        private Object[] resolveArgs(Method method, JobClient client, ActivatedJob job) {
            Object[] args = new Object[method.getParameterCount()];
            Class<?>[] parameterTypes = method.getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(JobClient.class)) {
                    args[i] = client;
                } else if (parameterTypes[i].equals(ActivatedJob.class)) {
                    args[i] = job;
                } else if (parameterTypes[i].equals(Map.class)) {
                    args[i] = job.getVariablesAsMap();
                } else {
                    // Try to map to POJO
                    try {
                        args[i] = objectMapper.convertValue(job.getVariablesAsMap(), parameterTypes[i]);
                    } catch (Exception e) {
                        log.warn("Failed to map variables to type {}", parameterTypes[i].getName(), e);
                        args[i] = null;
                    }
                }
            }
            return args;
        }
    }
}
