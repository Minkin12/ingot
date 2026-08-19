package dev.minkin.ingot.ui.maxes.types;

import dev.minkin.ingot.engine.model.MajorLift;
import lombok.Getter;

@Getter
public class MaxEditRow {
    private final MajorLift lift;
    private final int currentValue;

    public MaxEditRow(MajorLift lift, int currentValue) {
        this.lift = lift;
        this.currentValue = currentValue;
    }

}
