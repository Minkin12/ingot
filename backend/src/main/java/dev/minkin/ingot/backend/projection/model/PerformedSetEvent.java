package dev.minkin.ingot.backend.projection.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformedSetEvent {
    private int weekNumber;
    private int dayNumber;
    private String exerciseName;
    private int setNumber;
    private String weightLbs;
    private int reps;
}
