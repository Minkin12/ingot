package dev.minkin.ingot.data.remote.types;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private UUID eventId;
    private String eventType;
    private String payload;
    private long completedAt;
}

