package dev.minkin.ingot.ui.maxes;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.engine.model.Maxes;

public class EditMaxesViewModel extends ViewModel {
    private final MutableLiveData<Map<MajorLift, Double>> currentMaxes = new MutableLiveData<>();
    private final ProgramRepository programRepo;
    private final ExecutorService executor;

    public EditMaxesViewModel(ProgramRepository programRepo, ExecutorService executor) {
        this.programRepo = programRepo;
        this.executor = executor;
        loadMaxes();
    }

    private void loadMaxes() {
        executor.execute(() -> {
            Maxes maxes = programRepo.getCurrentMaxes();
            Map<MajorLift, Double> map = new HashMap<>();
            for (MajorLift lift : MajorLift.values()) {
                map.put(lift, maxes.getMaxWeight(lift));
            }
            currentMaxes.postValue(map);
        });
    }

    public void updateMax(MajorLift lift, double newValue) {
        executor.execute(() -> {
            try {
                programRepo.recordNewMax(lift, newValue);
            } catch (JsonProcessingException e) {
                Log.e("EditMaxesVM", "Failed to record max for " + lift, e);
            }
            loadMaxes();
        });
    }

    public LiveData<Map<MajorLift, Double>> getCurrentMaxes() { return currentMaxes; }
}