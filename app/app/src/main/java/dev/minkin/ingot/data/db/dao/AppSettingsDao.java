package dev.minkin.ingot.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.minkin.ingot.data.db.entity.AppSettingsEntity;
import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;

@Dao
public interface AppSettingsDao {
    @Query("SELECT activeProgramId FROM app_settings WHERE id = 0")
    String getActiveProgramId();

    @Query("UPDATE app_settings SET activeProgramId = :programId WHERE id = 0")
    void setActiveProgramId(String programId);

    @Query("SELECT programId, jsonBlob FROM program_template")
    List<ProgramTemplateEntity> selectAllPrograms();

    @Query("SELECT lastPullSyncedAt FROM app_settings WHERE id = 0")
    Long getLastPullSyncedAt();

    @Query("UPDATE app_settings SET lastPullSyncedAt = :timestamp WHERE id = 0")
    void setLastPullSyncedAt(long timestamp);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void ensureRowExists(AppSettingsEntity entity);
}
