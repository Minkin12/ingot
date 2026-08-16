package dev.minkin.ingot.data.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import dev.minkin.ingot.data.db.entity.OutboxEntity;

@Dao
public interface OutboxDao {

    @Query("Select * from outbox order by createdAt asc")
    List<OutboxEntity> getAllQueuedEvents();

    @Query("Delete from outbox where eventId in (:eventIds)")
    void deleteQueuedEvent(List<String> eventIds);
}
