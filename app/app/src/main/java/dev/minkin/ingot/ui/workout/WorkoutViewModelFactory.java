package dev.minkin.ingot.ui.workout;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dev.minkin.ingot.AppContainer;

public class WorkoutViewModelFactory implements ViewModelProvider.Factory {
    AppContainer container;
    int weekNumber;
    int dayNumber;
    public WorkoutViewModelFactory(AppContainer container, int weekNumber, int dayNumber) {
        this.container = container;
        this.weekNumber = weekNumber;
        this.dayNumber = dayNumber;

    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new WorkoutViewModel(container.programRepository,container.workoutRepository, container.databaseExecutor, weekNumber, dayNumber );
    }
}
