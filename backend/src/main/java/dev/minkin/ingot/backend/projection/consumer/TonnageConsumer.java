package dev.minkin.ingot.backend.projection.consumer;

import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.projection.entity.TonnageEntity;
import dev.minkin.ingot.backend.projection.model.PerformedSetEvent;
import dev.minkin.ingot.backend.projection.repository.TonnageRepository;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class TonnageConsumer {
    private final TonnageRepository tonnageRepository;
    private final ObjectMapper objectMapper;

    //Unused stream info param structurally confirms nats setup runs before consumer setup
    public TonnageConsumer(Connection connection, JetStream jetStream, TonnageRepository tonnageRepository, ObjectMapper objectMapper, StreamInfo ingotEventStream) throws Exception {
        this.tonnageRepository = tonnageRepository;
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
                .durable("tonnage-projection")
                .maxDeliver(5)
                .build();
        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .stream("INGOT_EVENT_STREAM")
                .configuration(consumerConfig)
                .build();

        jetStream.subscribe("events.performed_set", dispatcher, handler, false, options);

    }

    private void processEvent(Message msg) {

        Event event = objectMapper.readValue(msg.getData(), Event.class);
        PerformedSetEvent performedSetEvent = objectMapper.readValue(event.getPayload(), PerformedSetEvent.class);

        int week = performedSetEvent.getWeekNumber();
        int day = performedSetEvent.getDayNumber();
        String exerciseName = performedSetEvent.getExerciseName();
        int setNumber = performedSetEvent.getSetNumber();
        int reps = performedSetEvent.getReps();
        String weightLbs = performedSetEvent.getWeightLbs();
        UUID eventId = event.getEventId();

        Optional<TonnageEntity> existing = tonnageRepository
                .findByWeekNumberAndDayNumberAndExerciseNameAndSetNumber(week, day, exerciseName, setNumber);

        TonnageEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
        } else {
            entity = new TonnageEntity();
        }

        entity.setWeekNumber(week);
        entity.setDayNumber(day);
        entity.setExerciseName(exerciseName);
        entity.setSetNumber(setNumber);
        entity.setWeightLbs(weightLbs);
        entity.setReps(reps);
        entity.setSourceEventId(eventId);

        tonnageRepository.save(entity);
    }
}
