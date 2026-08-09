package dev.minkin.ingot.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "program_template")
public class ProgramTemplateEntity {
    @PrimaryKey
    public int id;

    public String jsonBlob;


}
