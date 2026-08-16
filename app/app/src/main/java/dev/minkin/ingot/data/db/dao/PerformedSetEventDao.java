package dev.minkin.ingot.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;

@Dao
public abstract class PerformedSetEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPerformedSetEvent(PerformedSetEventEntity performedSetEventEntity);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertOutboxEvent(OutboxEntity outboxEntity);

    @Query("Select * from performed_set_event where weekNumber == :weekNumber and dayNumber == :dayNumber order by loggedAt desc")
    public abstract LiveData<List<PerformedSetEventEntity>> observePerformedSetsForSessionCoordinates(int weekNumber, int dayNumber);

    @Query("Select * from performed_set_event where exerciseName == :exerciseName order by loggedAt limit 6")
    public abstract List<PerformedSetEventEntity> retrievePerformedSetForExercise(String exerciseName);

    public abstract @Query("Select distinct weekNumber, dayNumber from performed_set_event " +
            "order by weekNumber, dayNumber")
    LiveData<List<SessionCoordinates>> observeStartedSessionCoordinates();

    @Transaction
    public void insertPerformedSetAndQueue(PerformedSetEventEntity performedSetEventEntity, OutboxEntity outboxEntity){
        insertPerformedSetEvent(performedSetEventEntity);
        insertOutboxEvent(outboxEntity);
    }
}
