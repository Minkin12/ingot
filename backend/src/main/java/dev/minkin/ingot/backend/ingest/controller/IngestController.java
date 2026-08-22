package dev.minkin.ingot.backend.ingest.controller;

import dev.minkin.ingot.backend.ingest.model.BatchInsertResults;
import dev.minkin.ingot.backend.ingest.model.Event;
import dev.minkin.ingot.backend.ingest.model.EventBatchRequest;
import dev.minkin.ingot.backend.ingest.repository.EventRepository;
import dev.minkin.ingot.backend.ingest.service.IngestService;
import io.nats.client.JetStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

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
    IngestService ingestService;


    public IngestController(EventRepository eventRepository, JetStream jetStream, ObjectMapper objectMapper, IngestService ingestService) {
        this.eventRepository = eventRepository;
        this.jetStream = jetStream;
        this.objectMapper = objectMapper;
        this.ingestService = ingestService;
    }

    @PostMapping("/batchInsertEvents")
    public ResponseEntity<BatchInsertResults> batchInsertEvents(@RequestBody EventBatchRequest batchRequest) {
        List<UUID> completedEvents = new ArrayList<>();
        List<UUID> failedEvents = new ArrayList<>();
        for (Event event : batchRequest.getEvents()) {
             if (ingestService.processEvent(event)) {
                 completedEvents.add(event.getEventId());
             } else  {
                 failedEvents.add(event.getEventId());
             }
        }

        return ResponseEntity.ok().body(new BatchInsertResults(completedEvents, failedEvents));
    }


}
