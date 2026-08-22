package dev.minkin.ingot.backend.ingest.service;

import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.ingest.repository.EventRepository;
import io.nats.client.JetStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class IngestService {
    EventRepository eventRepository;
    JetStream jetStream;
    ObjectMapper objectMapper;


    public IngestService(EventRepository eventRepository, JetStream jetStream, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.jetStream = jetStream;
        this.objectMapper = objectMapper;
    }

    public Boolean processEvent(Event event) {
        boolean eventProcessed = false;
        try {
            boolean inserted = eventRepository.insertEventIfNew(event);
            if (inserted) {
                String eventString = objectMapper.writeValueAsString(event);
                switch (event.getEventType()) {
                    case "performed_set":
                        jetStream.publish("events.performed_set", eventString.getBytes(StandardCharsets.UTF_8));
                        eventProcessed = true;
                        break;
                    case "workout_completed":
                        jetStream.publish("events.workout_completed", eventString.getBytes(StandardCharsets.UTF_8));
                        eventProcessed = true;
                        break;
                    case "training_max_updated":
                        jetStream.publish("events.training_max_updated", eventString.getBytes(StandardCharsets.UTF_8));
                        eventProcessed = true;
                        break;
                    default:
                        log.warn("Unknown event type: {}", event.getEventType());
                        break;

                }
            }
        } catch (Exception e) {
            log.error("Error while inserting event: {}", event.getEventId(), e);
        }
        return eventProcessed;
    }

}
