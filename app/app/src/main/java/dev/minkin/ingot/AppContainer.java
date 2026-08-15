package dev.minkin.ingot;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.room.Room;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.minkin.ingot.data.db.IngotDatabase;
import dev.minkin.ingot.data.remote.IngotApi;
import dev.minkin.ingot.data.repo.HistoryRepository;
import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.WorkoutRepository;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AppContainer {
    public final ExecutorService databaseExecutor;
    public final ProgramRepository programRepository;
    public final WorkoutRepository workoutRepository;
    public final HistoryRepository historyRepository;

    public AppContainer(Context context) throws IOException {
        IngotDatabase db = Room.databaseBuilder(context, IngotDatabase.class, "ingot_db").fallbackToDestructiveMigration().build();
        AssetManager assets = context.getAssets();

        databaseExecutor = Executors.newFixedThreadPool(4);

        programRepository = new ProgramRepository(
                db.programTemplateDao(), db.trainingMaxDao(), databaseExecutor, assets);
        workoutRepository = new WorkoutRepository(
                db.performedSetEventDao(), db.workoutCompletedEventDao(), databaseExecutor);
        databaseExecutor.execute(() -> {
            try {
                programRepository.ensureSeeded();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        historyRepository = new HistoryRepository(buildIngotApi(), databaseExecutor);
    }
    private IngotApi buildIngotApi() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.INGOT_API_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        return retrofit.create(IngotApi.class);
    }
}
