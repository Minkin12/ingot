package dev.minkin.ingot.backend.projection.model;

public interface TonnageWeekSummary {
    int getWeekNumber();
    int getDayNumber();
    String getExerciseName();
    double getTotalTonnage();
    long getSetCount();
}
