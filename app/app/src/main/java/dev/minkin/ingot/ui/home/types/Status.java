package dev.minkin.ingot.ui.home.types;

import java.util.Set;

import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;

public enum Status {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    public static Status deriveStatus(Set<SessionCoordinates> completed, Set<SessionCoordinates> started,
                                      int week, int day) {
        SessionCoordinates coords = new SessionCoordinates(week, day);
        if (completed.contains(coords)) return Status.COMPLETED;
        if (started.contains(coords)) return Status.IN_PROGRESS;
        return Status.NOT_STARTED;
    }
}
