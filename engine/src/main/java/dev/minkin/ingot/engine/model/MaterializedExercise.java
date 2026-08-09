package dev.minkin.ingot.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public final class MaterializedExercise {
    private final Exercise exercise;  // the intent, from the template
    private final String load;                // resolved: 220.0, or null for blank accessories



}
