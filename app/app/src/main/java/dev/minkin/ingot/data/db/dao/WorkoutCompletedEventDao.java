package dev.minkin.ingot.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;

@Dao
public abstract class WorkoutCompletedEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertWorkoutCompletedEvent(WorkoutCompletedEventEntity workoutCompletedEventEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertOutboxEvent(OutboxEntity outboxEntity);

    @Query("Select distinct weekNumber, dayNumber from workout_completed_event order by weekNumber, dayNumber")
    public abstract LiveData<List<SessionCoordinates>> observeCompletedSessionCoordinates();

    @Query("Select * from workout_completed_event where weekNumber == :weekNumber and dayNumber == :dayNumber")
    public abstract WorkoutCompletedEventEntity selectCompletedSessionBySessionCoordinates(int weekNumber, int dayNumber);

    @Transaction
    public void insertWorkoutCompletedAndQueue(WorkoutCompletedEventEntity workoutCompletedEventEntity, OutboxEntity outboxEntity){
        insertWorkoutCompletedEvent(workoutCompletedEventEntity);
        insertOutboxEvent(outboxEntity);
    }
}
