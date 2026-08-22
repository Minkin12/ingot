package dev.minkin.ingot.backend.projection.consumer;

import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.ingest.service.IngestService;
import dev.minkin.ingot.backend.projection.entity.TrainingMaxHistoryEntity;
import dev.minkin.ingot.backend.projection.model.TestResult;
import dev.minkin.ingot.backend.projection.model.WorkoutCompletedEvent;
import dev.minkin.ingot.backend.projection.repository.TrainingMaxHistoryRepository;
import dev.minkin.ingot.engine.Rounding;
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
public class ProgressionEngineConsumer {
    private final TrainingMaxHistoryRepository trainingMaxHistoryRepository;
    private final IngestService ingestService;
    private final ObjectMapper objectMapper;

    //Unused stream info param structurally confirms nats setup runs before consumer setup
    public ProgressionEngineConsumer(Connection connection, JetStream jetStream, TrainingMaxHistoryRepository trainingMaxHistoryRepository, IngestService ingestService, ObjectMapper objectMapper, StreamInfo ingotEventStream) throws Exception {
        this.trainingMaxHistoryRepository = trainingMaxHistoryRepository;
        this.ingestService = ingestService;
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
                .durable("progression-engine")
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

        if (workoutCompletedEvent.getTestResults() == null || workoutCompletedEvent.getTestResults().isEmpty()) {
            log.info("No test results exist for eventId: {}, discarding", event.getEventId());
            return;
        }

        for (TestResult testResult : workoutCompletedEvent.getTestResults()) {
            Optional<TrainingMaxHistoryEntity> currentMax =
                    trainingMaxHistoryRepository.findTopByLiftOrderByAchievedAtDesc(testResult.getLift());

            double weight;
            try {
                weight = Double.parseDouble(testResult.getWeightLbs());
            } catch (NumberFormatException e) {
                log.warn("Invalid weight for test result: {}", testResult.getWeightLbs());
                continue;
            }

            double reps = testResult.getReps();
            double estimated1rm = reps != 1 ? Rounding.toNearestFive(weight * (1 + reps / 30)) : weight;

            if (currentMax.isPresent()) {
                double currentMaxWeight = Double.parseDouble(currentMax.get().getValueLbs());
                // if it's below 50% it's probably not real (mistyped) — otherwise still accept, even if lower
                if (estimated1rm < currentMaxWeight * .5) {
                    log.error("Weight too low for lift: {}, estimated {} vs current {}",
                            testResult.getLift(), estimated1rm, currentMaxWeight);
                    continue;
                }
            }

            TrainingMaxHistoryEntity newEntry = new TrainingMaxHistoryEntity();
            newEntry.setLift(testResult.getLift());
            newEntry.setValueLbs(String.valueOf(estimated1rm));
            newEntry.setAchievedAt(event.getCompletedAt());
            newEntry.setSourceEventId(event.getEventId());

            Event trainingMaxEvent = new Event();
            trainingMaxEvent.setEventId(UUID.randomUUID());
            trainingMaxEvent.setEventType("training_max_updated");
            trainingMaxEvent.setPayload(objectMapper.writeValueAsString(newEntry));
            trainingMaxEvent.setCompletedAt(System.currentTimeMillis());
            ingestService.processEvent(trainingMaxEvent);
        }
    }
}
