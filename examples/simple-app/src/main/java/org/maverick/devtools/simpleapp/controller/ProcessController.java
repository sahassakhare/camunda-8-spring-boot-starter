package org.maverick.devtools.simpleapp.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final ZeebeClient zeebeClient;

    @PostMapping("/deploy")
    public java.util.Map<String, Object> deployProcess() {
        DeploymentEvent event = zeebeClient.newDeployResourceCommand()
                .addResourceFromClasspath("process.bpmn")
                .send()
                .join();
        log.info("Deployment created: {}", event.getKey());

        return java.util.Map.of(
                "deploymentKey", event.getKey(),
                "processes", event.getProcesses().stream()
                        .map(metadata -> java.util.Map.of(
                                "bpmnProcessId", metadata.getBpmnProcessId(),
                                "version", metadata.getVersion(),
                                "processDefinitionKey", metadata.getProcessDefinitionKey(),
                                "resourceName", metadata.getResourceName()))
                        .collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping("/start")
    public String startProcess(@RequestBody Map<String, Object> variables) {
        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_Payment")
                .latestVersion()
                .variables(variables)
                .send()
                .join();
        log.info("Started process instance: {}", event.getProcessInstanceKey());
        return "Started process instance: " + event.getProcessInstanceKey();
    }
}
