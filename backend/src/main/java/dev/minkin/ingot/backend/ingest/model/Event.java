package dev.minkin.ingot.backend.ingest.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Event {
    private UUID eventId;
    private String eventType;
    private String payload;
    private long completedAt;
}
