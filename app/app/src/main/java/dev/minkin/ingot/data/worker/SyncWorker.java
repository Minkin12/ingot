package dev.minkin.ingot.data.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.remote.types.BatchInsertResults;
import dev.minkin.ingot.data.remote.types.Event;
import dev.minkin.ingot.data.remote.types.EventBatchRequest;
import dev.minkin.ingot.data.repo.OutboxQueueRepository;

public class SyncWorker extends Worker {

    private static final int MAX_RETRIES = 3;
    private static final int BATCH_SIZE = 100;

    public SyncWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("SyncWorker", "doWork() started");

        if (getRunAttemptCount() >= MAX_RETRIES) {
            return Result.failure();
        }

        AppContainer appContainer = ((IngotApplication) getApplicationContext()).container;
        OutboxQueueRepository outboxQueueRepository = appContainer.outboxQueueRepository;

        try {
            List<OutboxEntity> queuedEvents = outboxQueueRepository.getQueuedEvents();
            Log.d("SyncWorker", "Queued events count: " + (queuedEvents == null ? "null" : queuedEvents.size()));

            if (queuedEvents == null || queuedEvents.isEmpty()) {
                Log.i("SyncWorker", "No unsynced events found");
                return Result.success();
            }
            List<Event> events = new ArrayList<>();

            for (OutboxEntity oe : queuedEvents)
                events.add(Event.builder()
                        .eventId(UUID.fromString(oe.eventId))
                        .eventType(oe.eventType)
                        .payload(oe.payload)
                        .completedAt(oe.createdAt)
                        .build()
                );

            BatchInsertResults results = new BatchInsertResults(new ArrayList<>(), new ArrayList<>());
            for (int i = 0; i < events.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, events.size());
                EventBatchRequest eventBatchRequest = new EventBatchRequest(events.subList(i, end));

                try {
                    BatchInsertResults temp = outboxQueueRepository.batchInsertEvents(eventBatchRequest);
                    Log.d("SyncWorker", "Batch succeeded: " + temp.getCompletedEvents().size() + " completed, "
                            + temp.getFailedEvents().size() + " failed");

                    results.getCompletedEvents().addAll(temp.getCompletedEvents());
                    results.getFailedEvents().addAll(temp.getFailedEvents());
                } catch (IOException e) {
                    Log.e("SyncWorker", "Batch send failed with IOException", e);

                    return Result.retry();
                }
            }
            outboxQueueRepository.clearProcessedEvents(results.getCompletedEvents());
            Log.d("SyncWorker", "Cleared " + results.getCompletedEvents().size() + " processed events");

            return Result.success();

        } catch (Exception e) {
            Log.d("SyncWorker", "Object batch sync failed ", e);
            return Result.retry();

        }


    }
}
