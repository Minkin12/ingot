package dev.minkin.ingot.backend.projection.consumer;

import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.projection.entity.SessionHistoryEntity;
import dev.minkin.ingot.backend.projection.model.WorkoutCompletedEvent;
import dev.minkin.ingot.backend.projection.repository.SessionHistoryRepository;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class SessionHistoryConsumer {

    private final SessionHistoryRepository sessionHistoryRepository;
    private final ObjectMapper objectMapper;

    //Unused stream info param structurally confirms nats setup runs before consumer setup
    public SessionHistoryConsumer(Connection connection, JetStream jetStream, SessionHistoryRepository sessionHistoryRepository, ObjectMapper objectMapper, StreamInfo ingotEventStream) throws Exception {
        this.sessionHistoryRepository = sessionHistoryRepository;
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
                .durable("history-projection")
                .maxDeliver(5)
                .build();
        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .stream("INGOT_EVENT_STREAM")
                .configuration(consumerConfig)
                .build();

        jetStream.subscribe("events.workout_completed", dispatcher, handler, false, options);

    }

    private void processEvent(Message msg) {

        Event event = objectMapper.readValue(msg.getData(), Event.class);
        WorkoutCompletedEvent workoutCompletedEvent = objectMapper.readValue(event.getPayload(), WorkoutCompletedEvent.class);

        if (sessionHistoryRepository.existsById(event.getEventId())) {
            log.info("Session History already exists for eventId: {}, discarding", event.getEventId());
            return;

        }

        SessionHistoryEntity sessionHistoryEntity = new SessionHistoryEntity();
        sessionHistoryEntity.setEventId(event.getEventId());
        sessionHistoryEntity.setWeekNumber(workoutCompletedEvent.getWeekNumber());
        sessionHistoryEntity.setDayNumber(workoutCompletedEvent.getDayNumber());
        sessionHistoryEntity.setWorkoutLabel(workoutCompletedEvent.getWorkoutLabel());
        sessionHistoryEntity.setSessionNote(workoutCompletedEvent.getSessionNote());
        sessionHistoryEntity.setCompletedAt(event.getCompletedAt());
        sessionHistoryEntity.setRecordedAt(System.currentTimeMillis());

        sessionHistoryRepository.save(sessionHistoryEntity);
    }

}

