package dev.minkin.ingot.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "program_template")
public class ProgramTemplateEntity {
    @PrimaryKey
    @NonNull
    public String programId;

    public String jsonBlob;


}
