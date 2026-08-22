package dev.minkin.ingot.engine.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

import lombok.Getter;

@Getter
public class Day {
    String label;
    @JsonAlias("day_number")
    Integer dayNumber;
    String type;
    List<Exercise> exercises;
}
