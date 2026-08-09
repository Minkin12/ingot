package dev.minkin.ingot.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class MaterializedSession {
    private final int weekNumber;
    private final int dayNumber;
    private final String label;
    private final List<MaterializedExercise> exercises;
}
