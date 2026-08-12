package dev.minkin.ingot.ui.workout.types;

import java.util.List;

public class WorkoutUiState {
    private final String workoutLabel;
    private final List<ExerciseUiState> exercises;
    private final int activeExerciseIndex;
    private final String sessionNote;

    public WorkoutUiState(String workoutLabel, List<ExerciseUiState> exercises,
                          int activeExerciseIndex, String sessionNote) {
        this.workoutLabel = workoutLabel;
        this.exercises = exercises;
        this.activeExerciseIndex = activeExerciseIndex;
        this.sessionNote = sessionNote;
    }

    public String getWorkoutLabel() { return workoutLabel; }
    public List<ExerciseUiState> getExercises() { return exercises; }
    public int getActiveExerciseIndex() { return activeExerciseIndex; }
    public String getSessionNote() { return sessionNote; }
}