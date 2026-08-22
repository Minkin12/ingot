package dev.minkin.ingot.data.repo.types;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class WorkoutCompletedPayload {
    private int weekNumber;
    private int dayNumber;
    private String sessionNote;
    private String workoutLabel;
    private List<TestResult> testResults;
}
