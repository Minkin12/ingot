package dev.minkin.ingot.backend.projection.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutCompletedEvent {
    private int weekNumber;
    private int dayNumber;
    private String sessionNote;
    private String workoutLabel;
}
