package dev.minkin.ingot.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;

@Dao
public interface ProgramTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgram(ProgramTemplateEntity programTemplateEntity);

    @Query("Select * from program_template")
    ProgramTemplateEntity selectProgramTemplate();
}
