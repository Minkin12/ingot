package dev.minkin.ingot.engine.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class MaterializedSession {
    private final int weekNumber;
    private final int dayNumber;
    private final String label;
    private final String type;
    private final List<MaterializedExercise> exercises;
}
