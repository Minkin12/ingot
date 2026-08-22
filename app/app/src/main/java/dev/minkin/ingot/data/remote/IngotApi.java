package dev.minkin.ingot.data.remote;

import java.util.List;

import dev.minkin.ingot.data.remote.types.BatchInsertResults;
import dev.minkin.ingot.data.remote.types.EventBatchRequest;
import dev.minkin.ingot.data.remote.types.SessionHistoryEntry;
import dev.minkin.ingot.data.remote.types.TrainingMaxHistoryDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IngotApi {
    @GET("/api/v1/session-history")
    Call<List<SessionHistoryEntry>> getSessionHistory();

    @POST("/api/v1/batchInsertEvents")
    Call<BatchInsertResults> insertEvents(@Body EventBatchRequest eventBatchRequest);

    @GET("/api/v1/training-maxes/sync")
    Call<List<TrainingMaxHistoryDto>> getMaxesSince(@Query("since") Long since);
}
