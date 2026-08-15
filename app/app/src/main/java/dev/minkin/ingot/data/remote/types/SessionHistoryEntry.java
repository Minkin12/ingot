package dev.minkin.ingot.data.remote.types;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionHistoryEntry {
    private UUID eventId;
    private Integer weekNumber;
    private Integer dayNumber;
    private String workoutLabel;
    private String sessionNote;
    private Long completedAt;
    private Long recordedAt;
}