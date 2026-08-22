package dev.minkin.ingot.backend.projection.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    private String lift;
    private String weightLbs;
    private int reps;
}
