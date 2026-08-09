package dev.minkin.ingot.engine.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

import java.util.List;

@Getter
public class Day {
    String label;
    @JsonAlias("day_number")
    Integer dayNumber;
    List<Exercise> exercises;
}
