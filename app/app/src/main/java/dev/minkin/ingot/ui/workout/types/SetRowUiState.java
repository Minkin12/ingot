package dev.minkin.ingot.ui.workout.types;

import lombok.Getter;

@Getter
public class SetRowUiState {
    private final int setNumber;
    private final String weight;
    private final String reps;
    private final String note;
    private final boolean confirmed;

    public SetRowUiState(int setNumber, String weight, String reps, String note, boolean confirmed) {
        this.setNumber = setNumber;
        this.weight = weight;
        this.reps = reps;
        this.note = note;
        this.confirmed = confirmed;
    }

}