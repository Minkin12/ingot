package dev.minkin.ingot.backend.projection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tonnage_sets", schema = "ingot")
@Getter
@Setter
public class TonnageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    private Integer weekNumber;
    private Integer dayNumber;
    private String exerciseName;
    private String weightLbs;
    private Integer reps;
    private Integer setNumber;
    private UUID sourceEventId;
}
