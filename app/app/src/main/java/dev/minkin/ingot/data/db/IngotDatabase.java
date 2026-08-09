package dev.minkin.ingot.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;
import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;
import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.db.dao.PerformedSetEventDao;
import dev.minkin.ingot.data.db.dao.ProgramTemplateDao;
import dev.minkin.ingot.data.db.dao.TrainingMaxDao;
import dev.minkin.ingot.data.db.dao.WorkoutCompletedEventDao;

@Database(version = 1, entities = {TrainingMaxEntity.class, ProgramTemplateEntity.class, PerformedSetEventEntity.class, WorkoutCompletedEventEntity.class},exportSchema = false)
public abstract class IngotDatabase extends RoomDatabase {
    private static volatile IngotDatabase instance;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);
    public abstract TrainingMaxDao trainingMaxDao();
    public abstract ProgramTemplateDao programTemplateDao();
    public abstract PerformedSetEventDao performedSetEventDao();
    public abstract WorkoutCompletedEventDao workoutCompletedEventDao();

    public static synchronized IngotDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            IngotDatabase.class, "ingot_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }


}
