package dev.minkin.ingot.data.repo;

import java.util.List;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.remote.IngotApi;
import dev.minkin.ingot.data.remote.types.SessionHistoryEntry;
import retrofit2.Response;

public class HistoryRepository {
    private final IngotApi api;
    private final ExecutorService executor;

    public HistoryRepository(IngotApi ingotApi, ExecutorService executor){
        this.api = ingotApi;
        this.executor = executor;
    }
    public interface HistoryCallback {
        void onSuccess(List<SessionHistoryEntry> history);
        void onError(Throwable error);
    }

    public void getHistory(HistoryCallback callback) {
        executor.execute(() -> {
            try {
                Response<List<SessionHistoryEntry>> response = api.getSessionHistory().execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new RuntimeException(
                            "Request failed: " + response.code() + " " + response.message()));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

}
