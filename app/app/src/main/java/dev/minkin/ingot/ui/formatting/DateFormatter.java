package dev.minkin.ingot.ui.formatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateFormatter {
    private DateFormatter() {}

    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US);

    public static String formatCompletedAt(Long epochMillis) {
        if (epochMillis == null) return "";
        return FORMAT.format(new Date(epochMillis));
    }
}