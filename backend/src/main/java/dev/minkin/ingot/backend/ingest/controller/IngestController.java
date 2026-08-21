package dev.minkin.ingot.backend.ingest.controller;

import dev.minkin.ingot.backend.ingest.model.BatchInsertResults;
import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.ingest.model.EventBatchRequest;
import dev.minkin.ingot.backend.ingest.repository.EventRepository;
import io.nats.client.JetStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@Slf4j
public class IngestController {

    EventRepository eventRepository;
    JetStream jetStream;
    ObjectMapper objectMapper;


    public IngestController(EventRepository eventRepository, JetStream jetStream, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.jetStream = jetStream;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/batchInsertEvents")
    public ResponseEntity<BatchInsertResults> batchInsertEvents(@RequestBody EventBatchRequest batchRequest) {
        List<UUID> completedEvents = new ArrayList<>();
        List<UUID> failedEvents = new ArrayList<>();
        for (Event event : batchRequest.getEvents()) {
            try {
                eventRepository.insertEventIfNew(event);
                String eventString = objectMapper.writeValueAsString(event);
                switch (event.getEventType()) {
                    case "performed_set":
                        jetStream.publish("events.performed_set", eventString.getBytes(StandardCharsets.UTF_8));
                        completedEvents.add(event.getEventId());
                        break;
                    case "workout_completed":
                        jetStream.publish("events.workout_completed", eventString.getBytes(StandardCharsets.UTF_8));
                        completedEvents.add(event.getEventId());
                        break;
                    case "training_max_updated":
                        jetStream.publish("events.training_max_updated", eventString.getBytes(StandardCharsets.UTF_8));
                        completedEvents.add(event.getEventId());
                        break;
                    default:
                        log.warn("Unknown event type: {}", event.getEventType());
                        failedEvents.add(event.getEventId());
                        break;

                }
            } catch (Exception e) {
                log.error("Error while inserting event: {}", event.getEventId(), e);
                failedEvents.add(event.getEventId());
            }
        }

        return ResponseEntity.ok().body(new BatchInsertResults(completedEvents, failedEvents));
    }


}
