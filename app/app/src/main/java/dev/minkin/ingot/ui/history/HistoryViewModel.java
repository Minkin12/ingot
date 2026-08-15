package dev.minkin.ingot.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.minkin.ingot.data.remote.types.SessionHistoryEntry;
import dev.minkin.ingot.data.repo.HistoryRepository;

public class HistoryViewModel extends ViewModel {
    private final MutableLiveData<List<SessionHistoryEntry>> history = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(true);

    public HistoryViewModel(HistoryRepository repository) {
        repository.getHistory(new HistoryRepository.HistoryCallback() {
            @Override
            public void onSuccess(List<SessionHistoryEntry> result) {
                history.postValue(result);
                loading.postValue(false);
            }

            @Override
            public void onError(Throwable e) {
                error.postValue(e.getMessage());
                loading.postValue(false);
            }
        });
    }

    public LiveData<List<SessionHistoryEntry>> getHistory() {
        return history;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }
}