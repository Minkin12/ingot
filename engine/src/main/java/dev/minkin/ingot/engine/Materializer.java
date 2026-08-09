package dev.minkin.ingot.engine;

import dev.minkin.ingot.engine.model.*;

import java.util.ArrayList;
import java.util.List;

public class Materializer {

    public static MaterializedSession materialize(Program program, Maxes maxes, int weekNumber, int dayNumber) {
        Day day = program.getDay(weekNumber, dayNumber);
        if (day == null){
            throw new IllegalArgumentException(String.format("Day %d is null for week %d", dayNumber,weekNumber));
        }
        List<MaterializedExercise> materializedExerciseList = new ArrayList<>();

        for (Exercise exercise : day.getExercises()){
            if (exercise.getSourceLift() != null && !exercise.getSourceLift().isBlank()){
                MajorLift majorLift = MajorLift.fromJson(exercise.getSourceLift());
                double maxWeight = maxes.getMaxWeight(majorLift);

                Double calculatedWeight = Rounding.toNearestFive(maxWeight * exercise.getPctOneRepMax());


                materializedExerciseList.add(MaterializedExercise.builder()
                        .exercise(exercise)
                        .load(String.valueOf(calculatedWeight))
                        .build());
            } else {
                materializedExerciseList.add(MaterializedExercise.builder()
                        .exercise(exercise)
                        .build());
            }
        }
        return MaterializedSession.builder()
                .weekNumber(weekNumber)
                .dayNumber(dayNumber)
                .label(day.getLabel())
                .exercises(materializedExerciseList)
                .build();
    }
}
