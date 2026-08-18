package dev.minkin.ingot.ui.program;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dev.minkin.ingot.AppContainer;

public class ProgramPickerViewModelFactory implements ViewModelProvider.Factory {
    private final AppContainer container;

    public ProgramPickerViewModelFactory(AppContainer container) {
        this.container = container;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProgramPickerViewModel(container.programRepository, container.databaseExecutor);
    }
}