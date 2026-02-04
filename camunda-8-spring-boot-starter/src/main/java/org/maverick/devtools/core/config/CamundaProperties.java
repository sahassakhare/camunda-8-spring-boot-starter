package org.maverick.devtools.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "camunda.zeebe")
@Data
public class CamundaProperties {

    private Broker broker = new Broker();
    private Cloud cloud = new Cloud();
    private Security security = new Security();
    private Worker worker = new Worker();
    private Deployment deployment = new Deployment();

    @Data
    public static class Broker {
        private String gatewayAddress = "127.0.0.1:26500";
        private long keepAlive = 45000;
    }

    @Data
    public static class Cloud {
        private String clusterId;
        private String clientId;
        private String clientSecret;
        private String region = "bru-2";
        private String authUrl = "https://login.cloud.camunda.io/oauth/token";
    }

    @Data
    public static class Security {
        private boolean plaintext = false;
        private String overrideAuthority;
        private String certPath;
    }

    @Data
    public static class Worker {
        private String defaultType;
        private Integer threads; // Maps to maxJobsActive default
        private Integer maxJobsActive = 32;
        private Long pollInterval = 100L;
        private Long requestTimeout = 10000L;
        private boolean streamEnabled = false;
    }

    @Data
    public static class Deployment {
        private boolean enabled = true;
        private String resources = "classpath*:**/*.bpmn,classpath*:**/*.dmn,classpath*:**/*.form";
    }
}
