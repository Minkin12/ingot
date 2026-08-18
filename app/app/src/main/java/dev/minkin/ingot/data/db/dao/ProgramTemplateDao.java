package dev.minkin.ingot.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;

@Dao
public interface ProgramTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgram(ProgramTemplateEntity programTemplateEntity);

    @Query("Select * from program_template where programId == :programId")
    ProgramTemplateEntity selectProgramTemplate(String programId);

    @Query("SELECT programId FROM program_template")
    List<String> selectAllProgramIds();
}
