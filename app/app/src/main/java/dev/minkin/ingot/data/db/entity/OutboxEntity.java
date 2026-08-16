package dev.minkin.ingot.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outbox")
public class OutboxEntity {
    @PrimaryKey
    @NonNull
    public String eventId;

    public String eventType;
    public String payload;
    public long createdAt;
}
