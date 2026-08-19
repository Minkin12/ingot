package dev.minkin.ingot.ui.maxes;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dev.minkin.ingot.AppContainer;

public class EditMaxesViewModelFactory implements ViewModelProvider.Factory {
    AppContainer container;
    public EditMaxesViewModelFactory(AppContainer container) {
        this.container = container;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new EditMaxesViewModel(container.programRepository, container.databaseExecutor);
    }
}
