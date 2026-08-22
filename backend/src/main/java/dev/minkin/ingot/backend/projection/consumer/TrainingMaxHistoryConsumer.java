package dev.minkin.ingot.backend.projection.consumer;

import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.projection.entity.TrainingMaxHistoryEntity;
import dev.minkin.ingot.backend.projection.repository.TrainingMaxHistoryRepository;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class TrainingMaxHistoryConsumer {

    private final TrainingMaxHistoryRepository trainingMaxHistoryRepository;
    private final ObjectMapper objectMapper;

    public TrainingMaxHistoryConsumer(Connection connection, JetStream jetStream, TrainingMaxHistoryRepository trainingMaxHistoryRepository, ObjectMapper objectMapper, StreamInfo ingotEventStream) throws Exception {
        this.trainingMaxHistoryRepository = trainingMaxHistoryRepository;
        this.objectMapper = objectMapper;

        Dispatcher dispatcher = connection.createDispatcher();
        MessageHandler handler = (Message msg) -> {
            try {
                processEvent(msg);
                msg.ack();
            } catch (Exception e) {
                log.error("Event failed", e);
                msg.nak();
            }
        };
        ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
                .durable("training-max-history-projection")
                .maxDeliver(5)
                .build();
        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .stream("INGOT_EVENT_STREAM")
                .configuration(consumerConfig)
                .build();

        jetStream.subscribe("events.training_max_updated", dispatcher, handler, false, options);
    }

    private void processEvent(Message msg) {
        Event event = objectMapper.readValue(msg.getData(), Event.class);
        TrainingMaxHistoryEntity entity = objectMapper.readValue(event.getPayload(), TrainingMaxHistoryEntity.class);

        entity.setId(null);
        trainingMaxHistoryRepository.save(entity);
    }
}