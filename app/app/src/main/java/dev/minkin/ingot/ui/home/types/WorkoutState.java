package dev.minkin.ingot.ui.home.types;

import dev.minkin.ingot.engine.model.MaterializedSession;
import lombok.Value;

@Value
public class WorkoutState {
    MaterializedSession session;
    Status status;
}
