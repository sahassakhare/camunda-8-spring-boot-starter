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

## Modules

*   `camunda-spring-boot-starter-core`: Basic client configuration.
*   `camunda-spring-boot-starter-worker`: Worker annotations and processor.
*   `camunda-spring-boot-starter-deployment`: Deployment automation.
*   `camunda-spring-boot-starter-test`: Testing utilities.

## Metrics
The starter exposes the following metrics via Micrometer:
*   `camunda.job.success` (Counter)
*   `camunda.job.failure` (Counter)
*   `camunda.job.duration` (Timer)

## Building
Run `mvn clean install` to build all modules.
