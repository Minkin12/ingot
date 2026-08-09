package dev.minkin.ingot.engine.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

@Getter
public class Exercise {
    String name;
    @JsonAlias("warmup_sets")
    Integer warmupSets;
    @JsonAlias("working_sets")
    Integer workingSets;
    String reps;
    String load;
    @JsonAlias("pct_1rm")
    Double pctOneRepMax;
    @JsonAlias("source_lift")
    String sourceLift;
    String rpe;
    String rest;
    @JsonAlias("coach_notes")
    String coachNotes;
}