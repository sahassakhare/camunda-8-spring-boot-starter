# Camunda 8 Spring Boot Starter

Production-grade Spring Boot Starter for Camunda Platform 8.

## Features

*   **Auto-Configuration**: Zero-config setup for Zeebe Client.
*   **Worker Abstraction**: Annotation-based `@ZeebeWorker` registration.
*   **Deployment**: Automatic BPMN/DMN deployment on startup.
*   **Observability**: Integrated Micrometer metrics and OpenTelemetry tracing (MDC).
*   **Resilience**: Built-in support for retries and backpressure.
*   **Testing**: Testcontainers support helper.

## Getting Started

### 1. Add Dependency
```xml
<dependency>
    <groupId>com.example.camunda</groupId>
    <artifactId>camunda-8-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Configure `application.yml`
```yaml
camunda:
  zeebe:
    # Saas Configuration
    cloud:
      cluster-id: "your-cluster-id"
      client-id: "your-client-id"
      client-secret: "your-client-secret"
      region: "bru-2"
    
    # OR Self-Managed
    # broker:
    #   gateway-address: "localhost:26500"
```

### 3. Create a Worker
```java
@Component
public class PaymentWorker {
    @ZeebeWorker(type = "charge-card")
    public void chargeCard(Map<String, Object> variables) {
        // ...
    }
}
```

## Architecture

*   **`camunda-8-spring-boot-starter`**: The single production-grade library containing all core, worker, and deployment logic.
*   **`camunda-8-spring-boot-starter-test`**: Separate module for testing utilities (Testcontainers).
*   **`examples/simple-app`**: A reference implementation demonstrating best practices.

## Best Practices
This starter enforces several best practices out of the box:
*   **Strong Typing**: Automatic POJO mapping for variables.
*   **Error Handling**: Built-in support for `ZeebeBpmnError`.
*   **Observability**: Metrics and MDC logging pre-configured.

## Building & Running
1.  **Build the project**:
    ```bash
    mvn clean install
    ```
2.  **Run Local Cluster** (requires Docker/Podman):
    ```bash
    # Deploys Zeebe 8.4, Operate 8.4, and Elasticsearch
    podman-compose up -d
    ```
3.  **Run Example App**:
    ```bash
    cd examples/simple-app
    mvn spring-boot:run
    ```
