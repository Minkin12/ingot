package dev.minkin.ingot.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_completed_event",
        indices = {@Index(value = {"weekNumber", "dayNumber"}, unique = true)})
public class WorkoutCompletedEventEntity {

    // client UUID
    @PrimaryKey
    @NonNull
    public String eventId;

    // which session is being declared done
    public int weekNumber;
    public int dayNumber;

    // nullable — "gym packed, subbed leg press"
    public String sessionNote;

    public String workoutLabel;

    // epoch millis
    public long completedAt;
}
