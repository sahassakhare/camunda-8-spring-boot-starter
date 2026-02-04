package org.maverick.devtools.worker.config;

import org.maverick.devtools.core.config.CamundaProperties;
import org.maverick.devtools.core.config.ZeebeClientAutoConfiguration;
import org.maverick.devtools.worker.processor.ZeebeWorkerPostProcessor;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = ZeebeClientAutoConfiguration.class)
@ConditionalOnBean(ZeebeClient.class)
@EnableConfigurationProperties(CamundaProperties.class)
public class ZeebeWorkerAutoConfiguration {

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
    public org.maverick.devtools.worker.metrics.WorkerMetrics workerMetrics(
            io.micrometer.core.instrument.MeterRegistry registry) {
        return new org.maverick.devtools.worker.metrics.WorkerMetrics(registry);
    }

    @Bean
    public ZeebeWorkerPostProcessor zeebeWorkerPostProcessor(ZeebeClient zeebeClient, CamundaProperties properties,
            org.maverick.devtools.worker.metrics.WorkerMetrics metrics,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new ZeebeWorkerPostProcessor(zeebeClient, properties, metrics, objectMapper);
    }
}
