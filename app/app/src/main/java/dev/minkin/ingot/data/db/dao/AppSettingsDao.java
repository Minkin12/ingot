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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setActiveProgram(AppSettingsEntity entity);

    @Query("SELECT activeProgramId FROM app_settings WHERE id = 0")
    String getActiveProgramId();

    @Query("SELECT programId, jsonBlob FROM program_template")
    List<ProgramTemplateEntity> selectAllPrograms();
}
