package dev.minkin.ingot.engine;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.engine.model.Day;
import dev.minkin.ingot.engine.model.Exercise;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.engine.model.MaterializedExercise;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.engine.model.Maxes;
import dev.minkin.ingot.engine.model.Program;

public class Materializer {

    public static MaterializedSession materialize(Program program, Maxes maxes, int weekNumber, int dayNumber) {
        Day day = program.getDay(weekNumber, dayNumber);
        if (day == null) {
            throw new IllegalArgumentException(String.format("Day %d is null for week %d", dayNumber, weekNumber));
        }
        List<MaterializedExercise> materializedExerciseList = new ArrayList<>();

        for (Exercise exercise : day.getExercises()) {
            String load = null;

            if (exercise.getSourceLift() != null && !exercise.getSourceLift().isBlank()) {
                MajorLift majorLift = MajorLift.fromJson(exercise.getSourceLift());
                double maxWeight = maxes.getMaxWeight(majorLift);

                Double pct = exercise.getPctOneRepMax();

                if (pct == null) {
                    Integer cleanReps = parseCleanReps(exercise.getReps());
                    if (cleanReps != null && PercentageTable.hasEntryFor(cleanReps)) {
                        pct = PercentageTable.pctForReps(cleanReps);
                    }
                }

                if (pct != null) {
                    Double calculatedWeight = Rounding.toNearestFive(maxWeight * pct);
                    load = String.valueOf(calculatedWeight);
                }
            }

            materializedExerciseList.add(MaterializedExercise.builder()
                    .exercise(exercise)
                    .load(load)
                    .build());
        }

        return MaterializedSession.builder()
                .weekNumber(weekNumber)
                .dayNumber(dayNumber)
                .label(day.getLabel())
                .exercises(materializedExerciseList)
                .build();
    }

    private static Integer parseCleanReps(String reps) {
        if (reps == null) return null;
        String trimmed = reps.trim();
        if (trimmed.endsWith("+")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}