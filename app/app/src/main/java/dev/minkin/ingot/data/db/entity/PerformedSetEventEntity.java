package dev.minkin.ingot.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "performed_set_event",
        indices = {@Index({"exerciseName", "loggedAt"}),
        @Index(value = {"weekNumber", "dayNumber", "exerciseName", "setNumber"}, unique = true)})
public class PerformedSetEventEntity {

    @PrimaryKey
    @NonNull
    public String eventId;

    // Program coordinates of the session this set belongs to.
    public int weekNumber;
    public int dayNumber;

    // Exercise name as it appears in the template (join key for history/prefill).
    public String exerciseName;

    // 1-based position within the exercise.
    public int setNumber;

    public String weightLbs;
    public int reps;

    // Nullable free-text note for this set.
    public String note;

    // Epoch millis when the set was logged.
    public long loggedAt;

    // TODO Used later for workout completion events maybe
    public String type;
}
