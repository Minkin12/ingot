package dev.minkin.ingot.engine;

import java.util.Map;

public final class PercentageTable {
    private PercentageTable() {}

    // Not perfect but a good starting place if the program doesnt have a percentage
    private static final Map<Integer, Double> REPS_TO_PCT = Map.ofEntries(
        Map.entry(1, 1.00),
        Map.entry(2, 0.95),
        Map.entry(3, 0.90),
        Map.entry(4, 0.88),
        Map.entry(5, 0.85),
        Map.entry(6, 0.82),
        Map.entry(7, 0.79),
        Map.entry(8, 0.76),
        Map.entry(9, 0.74),
        Map.entry(10, 0.72),
        Map.entry(11, 0.69),
        Map.entry(12, 0.66)
    );

    public static double pctForReps(int reps) {
        Double pct = REPS_TO_PCT.get(reps);
        if (pct == null) {
            throw new IllegalArgumentException(
                    "No percentage defined for " + reps + " reps — table covers 1-12");
        }
        return pct;
    }

    public static boolean hasEntryFor(int reps) {
        return REPS_TO_PCT.containsKey(reps);
    }
}