package dev.minkin.ingot.backend.projection.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "session_history", schema = "ingot")
@Getter
@Setter
public class SessionHistoryEntity {
    @Id
    private UUID eventId;
    private Integer weekNumber;
    private Integer dayNumber;
    private String workoutLabel;
    private String sessionNote;
    private Long completedAt;
    private Long recordedAt;
}
