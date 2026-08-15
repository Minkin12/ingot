package dev.minkin.ingot.ui.formatting;

import java.util.Locale;

import dev.minkin.ingot.engine.model.Exercise;
import dev.minkin.ingot.engine.model.MaterializedExercise;

public class ExerciseFormatter {
    private ExerciseFormatter() {}

    public static String formatRow(MaterializedExercise me) {
        Exercise ex = me.getExercise();
        String loadText = (me.getLoad() != null)
                ? String.format(Locale.US, "%s lbs", me.getLoad())
                : "—";
        return String.format(Locale.US, "%s — %d×%s @ %s",
                ex.getName(), ex.getWorkingSets(), ex.getReps(), loadText);
    }
}
