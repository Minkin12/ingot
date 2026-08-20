package dev.minkin.ingot.backend.projection.consumer;

import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.projection.entity.PersonalRecordEntity;
import dev.minkin.ingot.backend.projection.model.PerformedSetEvent;
import dev.minkin.ingot.backend.projection.repository.PersonalRecordRepository;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Slf4j
@Component
public class PersonalRecordsConsumer {
    private final PersonalRecordRepository personalRecordRepository;
    private final ObjectMapper objectMapper;

    //Unused stream info param structurally confirms nats setup runs before consumer setup
    public PersonalRecordsConsumer(Connection connection, JetStream jetStream, PersonalRecordRepository personalRecordRepository, ObjectMapper objectMapper, StreamInfo ingotEventStream) throws Exception {
        this.personalRecordRepository = personalRecordRepository;
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
                .durable("personal-records-projection")
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
        double reps = performedSetEvent.getReps();

        double weight;
        try {
            weight = Double.parseDouble(performedSetEvent.getWeightLbs());
        } catch (NumberFormatException e) {
            log.error("Invalid weight in performedSetEvent", e);
            return;
        }

        double estimated1rm = weight * (1 + reps /30);
        log.info("Estimated 1rm: {}", estimated1rm);

        Optional<PersonalRecordEntity> existing = personalRecordRepository.findById(performedSetEvent.getExerciseName());
        if (existing.isEmpty() || estimated1rm > existing.get().getEstimated_1rm()){
            PersonalRecordEntity updatedRecord = new PersonalRecordEntity();
            updatedRecord.setExerciseName(performedSetEvent.getExerciseName());
            updatedRecord.setBestWeightLbs(performedSetEvent.getWeightLbs());
            updatedRecord.setBestReps(performedSetEvent.getReps());
            updatedRecord.setEstimated_1rm(Math.round(Double.parseDouble(performedSetEvent.getWeightLbs()) * (1 + performedSetEvent.getReps()/30.0)));
            updatedRecord.setAchievedAt(event.getCompletedAt());
            updatedRecord.setSourceEventId(event.getEventId());
            personalRecordRepository.save(updatedRecord);
        }
    }
}
