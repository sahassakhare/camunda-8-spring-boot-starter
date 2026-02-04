package org.maverick.devtools.test.context;

import org.maverick.devtools.test.container.CamundaContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class CamundaTestContainerConfiguration {

    @Bean
    @ServiceConnection(name = "zeebe") // This would need a custom connection detail factory or manual property mapping
    public GenericContainer<?> camundaContainer() {
        return new CamundaContainer().withReuse(true);
    }
}
