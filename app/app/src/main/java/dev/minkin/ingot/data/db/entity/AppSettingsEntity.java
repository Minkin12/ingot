package dev.minkin.ingot.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettingsEntity {
    @PrimaryKey
    public int id = 0;

    public String activeProgramId;
    public Long lastPullSyncedAt;
}

