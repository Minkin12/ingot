package dev.minkin.ingot.backend.configuration;

import io.nats.client.*;
import io.nats.client.api.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration
public class NatsJetStreamConfiguration {

    @Value("${nats.url:nats://localhost:4222}")
    private String natsUrl;

    @Bean
    public Connection natsConnection() throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(natsUrl)
                .maxReconnects(-1)
                .connectionTimeout(Duration.ofSeconds(5))
                .build();
        return Nats.connect(options);
    }

    @Bean
    public JetStreamManagement jetStreamManagement(Connection connection) throws IOException {
        return connection.jetStreamManagement();
    }

    @Bean
    public JetStream jetStream(Connection connection) throws IOException {
        return connection.jetStream();
    }

    @Bean
    public ApplicationRunner jetStreamInfrastructureSetup(JetStreamManagement jsm) {
        return args -> {
            log.debug("running the infra setup");
            try {
                StreamConfiguration streamConfig = StreamConfiguration.builder()
                        .name("INGOT_EVENT_STREAM")
                        .subjects("events.*")
                        .storageType(StorageType.File)
                        .retentionPolicy(RetentionPolicy.Limits)
                        .maxAge(Duration.ofDays(30))
                        .build();

                jsm.addStream(streamConfig);

                ;
                log.info("Infra setup completed");
            } catch (Exception e) {
                log.debug("Failed to provision NATS infrastructure: {}", e.getMessage());
            }
        };
    }
}
