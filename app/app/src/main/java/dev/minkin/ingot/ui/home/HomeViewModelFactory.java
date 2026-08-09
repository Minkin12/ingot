package dev.minkin.ingot.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dev.minkin.ingot.AppContainer;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final AppContainer container;

    public HomeViewModelFactory(AppContainer container) {
        this.container = container;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(container.programRepository, container.workoutRepository, container.databaseExecutor);
    }
}