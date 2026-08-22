package dev.minkin.ingot.backend.projection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "training_max_history")
@Getter
@Setter
public class TrainingMaxHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private String lift;
    private String valueLbs;
    private long achievedAt;
    private UUID sourceEventId;
}