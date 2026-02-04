package com.example.camunda.deployment.config;

import com.example.camunda.core.config.CamundaProperties;
import com.example.camunda.core.config.ZeebeClientAutoConfiguration;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;

@AutoConfiguration(after = ZeebeClientAutoConfiguration.class)
@ConditionalOnBean(ZeebeClient.class)
@EnableConfigurationProperties(CamundaProperties.class)
@ConditionalOnProperty(prefix = "camunda.zeebe.deployment", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DeploymentAutoConfiguration {

    private final ZeebeClient zeebeClient;
    private final CamundaProperties properties;
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @PostConstruct
    public void deployResources() {
        String[] patterns = properties.getDeployment().getResources().split(",");

        for (String pattern : patterns) {
            try {
                Resource[] resources = resourceResolver.getResources(pattern);
                if (resources.length > 0) {
                    deploy(resources);
                }
            } catch (IOException e) {
                log.error("Failed to resolve resources for pattern: {}", pattern, e);
            }
        }
    }

    private void deploy(Resource[] resources) {
        DeployResourceCommandStep1.DeployResourceCommandStep2 deployCommand = null;

        for (Resource resource : resources) {
            try {
                if (deployCommand == null) {
                    deployCommand = zeebeClient.newDeployResourceCommand()
                            .addResourceStream(resource.getInputStream(), resource.getFilename());
                } else {
                    deployCommand = deployCommand.addResourceStream(resource.getInputStream(), resource.getFilename());
                }
                log.info("Adding resource to deployment: {}", resource.getFilename());
            } catch (IOException e) {
                log.error("Failed to read resource: {}", resource.getFilename(), e);
            }
        }

        if (deployCommand != null) {
            deployCommand.send().whenComplete((response, throwable) -> {
                if (throwable != null) {
                    log.error("Deployment failed", throwable);
                } else {
                    log.info("Deployment successful. Key: {}", response.getKey());
                    response.getProcesses().forEach(process -> log.info("Deployed process {} version {}",
                            process.getBpmnProcessId(), process.getVersion()));
                }
            });
        }
    }
}
