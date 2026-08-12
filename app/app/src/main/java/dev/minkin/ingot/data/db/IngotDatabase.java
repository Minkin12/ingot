package dev.minkin.ingot.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;
import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;
import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.db.dao.PerformedSetEventDao;
import dev.minkin.ingot.data.db.dao.ProgramTemplateDao;
import dev.minkin.ingot.data.db.dao.TrainingMaxDao;
import dev.minkin.ingot.data.db.dao.WorkoutCompletedEventDao;

@Database(version = 2, entities = {TrainingMaxEntity.class, ProgramTemplateEntity.class, PerformedSetEventEntity.class, WorkoutCompletedEventEntity.class},exportSchema = false)
public abstract class IngotDatabase extends RoomDatabase {
    public abstract TrainingMaxDao trainingMaxDao();
    public abstract ProgramTemplateDao programTemplateDao();
    public abstract PerformedSetEventDao performedSetEventDao();
    public abstract WorkoutCompletedEventDao workoutCompletedEventDao();
}
