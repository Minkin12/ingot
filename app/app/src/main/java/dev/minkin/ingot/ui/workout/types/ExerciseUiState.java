package dev.minkin.ingot.ui.workout.types;

import java.util.List;

import lombok.Getter;

@Getter
public class ExerciseUiState {
    private final String exerciseName;
    private final String targetSummary;
    private final String lastTimeSummary; // "30 × 10,10,10 — Pretty light" or null
    private final List<SetRowUiState> sets;

    public ExerciseUiState(String exerciseName, String targetSummary,
                           String lastTimeSummary, List<SetRowUiState> sets) {
        this.exerciseName = exerciseName;
        this.targetSummary = targetSummary;
        this.lastTimeSummary = lastTimeSummary;
        this.sets = sets;
    }

}