package dev.minkin.ingot.data.remote.types;

import java.util.UUID;

import lombok.Data;

@Data
public class Event {
    private UUID eventId;
    private String eventType;
    private String payload;
    private long completedAt;
}

