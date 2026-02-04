package com.example.camunda.worker.config;

import com.example.camunda.core.config.CamundaProperties;
import com.example.camunda.core.config.ZeebeClientAutoConfiguration;
import com.example.camunda.worker.processor.ZeebeWorkerPostProcessor;
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
    public com.example.camunda.worker.metrics.WorkerMetrics workerMetrics(
            io.micrometer.core.instrument.MeterRegistry registry) {
        return new com.example.camunda.worker.metrics.WorkerMetrics(registry);
    }

    @Bean
    public ZeebeWorkerPostProcessor zeebeWorkerPostProcessor(ZeebeClient zeebeClient, CamundaProperties properties,
            com.example.camunda.worker.metrics.WorkerMetrics metrics,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new ZeebeWorkerPostProcessor(zeebeClient, properties, metrics, objectMapper);
    }
}
