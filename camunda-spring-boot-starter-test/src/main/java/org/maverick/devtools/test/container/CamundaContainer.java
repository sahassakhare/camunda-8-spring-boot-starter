package org.maverick.devtools.test.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class CamundaContainer extends GenericContainer<CamundaContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("camunda/zeebe:8.5.0");
    private static final int ZEEBE_PORT = 26500;

    public CamundaContainer() {
        this(DEFAULT_IMAGE_NAME);
    }

    public CamundaContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(ZEEBE_PORT);
        withEnv("ZEEBE_BROKER_EXPORTERS_ELASTICSEARCH_className", "io.camunda.zeebe.exporter.ElasticsearchExporter");
        // Simplified setup for starter testing, users may want more complex topology
        waitingFor(Wait.forLogMessage(".*Broker is ready!.*\\n", 1));
    }

    public String getGatewayAddress() {
        return getHost() + ":" + getMappedPort(ZEEBE_PORT);
    }
}
