package dev.minkin.ingot;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.room.Room;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.minkin.ingot.data.db.IngotDatabase;
import dev.minkin.ingot.data.remote.IngotApi;
import dev.minkin.ingot.data.repo.HistoryRepository;
import dev.minkin.ingot.data.repo.OutboxQueueRepository;
import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.WorkoutRepository;
import dev.minkin.ingot.data.worker.SyncWorker;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AppContainer {
    public final ExecutorService databaseExecutor;
    public final ProgramRepository programRepository;
    public final WorkoutRepository workoutRepository;
    public final HistoryRepository historyRepository;
    public final OutboxQueueRepository outboxQueueRepository;
    public final IngotApi ingotApi;

    public AppContainer(Context context) throws IOException {
        IngotDatabase db = Room.databaseBuilder(context, IngotDatabase.class, "ingot_db").fallbackToDestructiveMigration().build();
        AssetManager assets = context.getAssets();

        databaseExecutor = Executors.newFixedThreadPool(4);

        programRepository = new ProgramRepository(
                db.programTemplateDao(), db.trainingMaxDao(),db.appSettingsDao(), databaseExecutor, assets);
        workoutRepository = new WorkoutRepository(
                db.performedSetEventDao(), db.workoutCompletedEventDao(), databaseExecutor);
        databaseExecutor.execute(() -> {
            try {
                programRepository.ensureSeeded();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ingotApi = buildIngotApi();

        historyRepository = new HistoryRepository(ingotApi, databaseExecutor);

        outboxQueueRepository = new OutboxQueueRepository(ingotApi, db.outboxDao());

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "Sync_queued_events",
                ExistingPeriodicWorkPolicy.KEEP,
                buildWorkRequest()
        );

        // One time used for testing
//        OneTimeWorkRequest nextRun = new OneTimeWorkRequest.Builder(SyncWorker.class)
//                .setInitialDelay(5, java.util.concurrent.TimeUnit.SECONDS)
//                .build();
//        WorkManager.getInstance(context).enqueue(nextRun);
    }

    private IngotApi buildIngotApi() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(false)
//                .connectionPool(new okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.INGOT_API_BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        return retrofit.create(IngotApi.class);
    }

    private PeriodicWorkRequest buildWorkRequest() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        return new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                45, TimeUnit.MINUTES,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, Duration.ofMinutes(1))
                .build();


    }
}
