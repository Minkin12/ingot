package dev.minkin.ingot.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;

@Dao
public interface PerformedSetEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPerformedSetEvent(PerformedSetEventEntity performedSetEventEntity);

    @Query("Select * from performed_set_event where weekNumber == :weekNumber and dayNumber == :dayNumber order by loggedAt desc")
    LiveData<List<PerformedSetEventEntity>> observePerformedSetsForSessionCoordinates(int weekNumber, int dayNumber);

    @Query("Select * from performed_set_event where exerciseName == :exerciseName order by loggedAt limit 6")
    List<PerformedSetEventEntity> retrievePerformedSetForExercise(String exerciseName);

    @Query("Select distinct weekNumber, dayNumber from performed_set_event " +
            "order by weekNumber, dayNumber")
    LiveData<List<SessionCoordinates>> observeStartedSessionCoordinates();
}
