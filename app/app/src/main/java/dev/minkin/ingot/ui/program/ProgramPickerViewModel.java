package dev.minkin.ingot.ui.program;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.types.ProgramSummary;

public class ProgramPickerViewModel extends ViewModel {
    private final MutableLiveData<List<ProgramSummary>> programs = new MutableLiveData<>();
    private final MutableLiveData<String> activeProgramId = new MutableLiveData<>();
    private final ProgramRepository programRepo;

    public ProgramPickerViewModel(ProgramRepository programRepo, ExecutorService executor) {
        this.programRepo = programRepo;
        executor.execute(() -> {
            try {
                programs.postValue(programRepo.listAvailablePrograms());
                activeProgramId.postValue(programRepo.getActiveProgramId());
            } catch (IOException e) {
                Log.e("ProgramPickerVM", "Failed to load programs", e);
            }
        });
    }

    public void selectProgram(String programId) {
        programRepo.setActiveProgram(programId);
        activeProgramId.postValue(programId);
    }

    public LiveData<List<ProgramSummary>> getPrograms() {
        return programs;
    }

    public LiveData<String> getActiveProgramId() {
        return activeProgramId;
    }
}
