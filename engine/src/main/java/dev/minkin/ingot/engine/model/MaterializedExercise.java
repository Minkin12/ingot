package dev.minkin.ingot.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public final class MaterializedExercise {
    private final Exercise exercise;  // the intent, from the template
    private final Double load;                // resolved: 220.0, or null for blank accessories



}
