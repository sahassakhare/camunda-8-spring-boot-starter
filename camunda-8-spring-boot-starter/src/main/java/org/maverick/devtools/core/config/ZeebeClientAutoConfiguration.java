package org.maverick.devtools.core.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(CamundaProperties.class)
@ConditionalOnClass(ZeebeClient.class)
@RequiredArgsConstructor
@Slf4j
public class ZeebeClientAutoConfiguration {

    private final CamundaProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public ZeebeClient zeebeClient() {
        log.info("Configuring Zeebe Client...");
        ZeebeClientBuilder builder = ZeebeClient.newClientBuilder();

        configureConnection(builder);
        configureAuthentication(builder);

        ZeebeClient client = builder.build();
        log.info("Zeebe Client successfully created and connected to: {}", properties.getCloud().getClusterId() != null
                ? properties.getCloud().getClusterId()
                : properties.getBroker().getGatewayAddress());
        return client;
    }

    private void configureConnection(ZeebeClientBuilder builder) {
        if (properties.getCloud().getClusterId() != null) {
            // SaaS Configuration
            String address = properties.getCloud().getClusterId() + "." + properties.getCloud().getRegion()
                    + ".zeebe.camunda.io:443";
            builder.gatewayAddress(address);
        } else {
            // Self-Managed Configuration
            builder.gatewayAddress(properties.getBroker().getGatewayAddress());
        }

        builder.keepAlive(Duration.ofMillis(properties.getBroker().getKeepAlive()));
    }

    private void configureAuthentication(ZeebeClientBuilder builder) {
        if (properties.getCloud().getClientId() != null && properties.getCloud().getClientSecret() != null) {
            // OAuth for SaaS or Identity
            OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
                    .authorizationServerUrl(properties.getCloud().getAuthUrl())
                    .audience(properties.getCloud().getClusterId() + "." + properties.getCloud().getRegion()
                            + ".zeebe.camunda.io")
                    .clientId(properties.getCloud().getClientId())
                    .clientSecret(properties.getCloud().getClientSecret())
                    .build();
            builder.credentialsProvider(credentialsProvider);
        } else if (properties.getSecurity().isPlaintext()) {
            builder.usePlaintext();
        }
    }
}
