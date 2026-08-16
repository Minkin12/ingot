package dev.minkin.ingot.data.repo;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.minkin.ingot.data.db.dao.OutboxDao;
import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.remote.IngotApi;
import dev.minkin.ingot.data.remote.types.BatchInsertResults;
import dev.minkin.ingot.data.remote.types.Event;
import dev.minkin.ingot.data.remote.types.EventBatchRequest;
import retrofit2.Response;

public class OutboxQueueRepository {
    private final IngotApi api;
    private final OutboxDao outboxDao;

    public OutboxQueueRepository(IngotApi api, OutboxDao outboxDao) {
        this.api = api;
        this.outboxDao = outboxDao;
    }

    public List<OutboxEntity> getQueuedEvents() {
        return outboxDao.getAllQueuedEvents();
    }

    public void clearProcessedEvents(List<String> eventIds) {
        outboxDao.deleteQueuedEvent(eventIds);
    }

    public BatchInsertResults batchInsertEvents(EventBatchRequest eventBatchRequest) throws IOException {
        Response<BatchInsertResults> response = api.insertEvents(eventBatchRequest).execute();
        List<String> successfulEvents = new ArrayList<>();
        List<String> failedEvents = new ArrayList<>();

        if (response.isSuccessful() && response.body() != null){
            successfulEvents.addAll(response.body().getCompletedEvents());
            failedEvents.addAll(response.body().getFailedEvents());
        } else {
            failedEvents.addAll(eventBatchRequest.getEvents().stream().map(Event::getEventId).map(UUID::toString).toList());
            assert response.errorBody() != null;
            Log.d("EventInsertFailure", String.format("Event Insert Failed, Response code: %s, Error body %s", response.code(), response.errorBody()));
        }
        return new BatchInsertResults(successfulEvents, failedEvents);
    }

}
