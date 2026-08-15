package dev.minkin.ingot.ui.history;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dev.minkin.ingot.AppContainer;

public class HistoryViewModelFactory implements ViewModelProvider.Factory {
    private final AppContainer container;

    public HistoryViewModelFactory(AppContainer container) {
        this.container = container;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HistoryViewModel(container.historyRepository);
    }
}