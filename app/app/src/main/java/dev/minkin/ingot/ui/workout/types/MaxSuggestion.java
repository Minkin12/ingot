package dev.minkin.ingot.ui.workout.types;

import dev.minkin.ingot.engine.model.MajorLift;
import lombok.Getter;

@Getter
public class MaxSuggestion {
    private final MajorLift lift;
    private final double estimated1RM;
    private final double currentMax;

    public MaxSuggestion(MajorLift lift, double estimated1RM, double currentMax) {
        this.lift = lift;
        this.estimated1RM = estimated1RM;
        this.currentMax = currentMax;
    }

}
