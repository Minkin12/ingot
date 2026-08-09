package dev.minkin.ingot;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.minkin.ingot.data.db.IngotDatabase;
import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.WorkoutRepository;

public class AppContainer {
    public final ExecutorService databaseExecutor;
    public final ProgramRepository programRepository;
    public final WorkoutRepository workoutRepository;

    public AppContainer(Context context) {
        IngotDatabase db = Room.databaseBuilder(context, IngotDatabase.class, "ingot.db").build();
        AssetManager assets = context.getAssets();

        databaseExecutor = Executors.newFixedThreadPool(4);

        programRepository = new ProgramRepository(
                db.programTemplateDao(), db.trainingMaxDao(), databaseExecutor, assets);
        workoutRepository = new WorkoutRepository(
                db.performedSetEventDao(), db.workoutCompletedEventDao(), databaseExecutor);
    }
}
