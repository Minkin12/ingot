package dev.minkin.ingot.backend.projection.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "personal_records", schema = "ingot")
@Getter
@Setter
public class PersonalRecordEntity {
    @Id
    private String exerciseName;
    private String bestWeightLbs;
    private Integer bestReps;
    private Long estimated_1rm;
    private Long achievedAt;
    private UUID sourceEventId;
}
