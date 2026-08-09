package dev.minkin.ingot.engine.model;

import lombok.Getter;

import java.util.List;

@Getter
public class Week {
    Integer number;
    String label;
    List<Day> days;
}
