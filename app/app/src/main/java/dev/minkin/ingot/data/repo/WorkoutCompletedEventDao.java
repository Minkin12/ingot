package dev.minkin.ingot.data.repo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;

@Dao
public interface WorkoutCompletedEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertWorkoutCompletedEvent(WorkoutCompletedEventEntity workoutCompletedEventEntity);

    @Query("Select distinct weekNumber, dayNumber from workout_completed_event order by weekNumber, dayNumber")
    LiveData<List<SessionCoordinates>> observeCompletedSessionCoordinates();

    @Query("Select * from workout_completed_event where weekNumber == :weekNumber and dayNumber == :dayNumber")
    WorkoutCompletedEventEntity selectCompletedSessionBySessionCoordinates(int weekNumber, int dayNumber);
}
