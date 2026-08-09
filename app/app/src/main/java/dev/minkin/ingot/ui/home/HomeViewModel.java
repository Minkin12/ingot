package dev.minkin.ingot.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.engine.model.MaterializedSession;

public class HomeViewModel extends ViewModel {

    private final ProgramRepository programRepository;
    private final ExecutorService executor;

    private final MutableLiveData<MaterializedSession> session = new MutableLiveData<>();

    public HomeViewModel(@NonNull ProgramRepository programRepository,
                         @NonNull ExecutorService executor) {
        this.programRepository = programRepository;
        this.executor = executor;
        loadUpNext();
    }

    public LiveData<MaterializedSession> getSession() {
        return session;
    }

    private void loadUpNext() {
        executor.execute(() -> {
            // todo hardcoded 1 day for now
            MaterializedSession result = null;
            try {
                result = programRepository.materializeSession(1, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            session.postValue(result);
        });
    }
}
