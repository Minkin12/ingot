package dev.minkin.ingot.ui.history;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.ui.history.adapters.HistoryAdapter;

public class HistoryActivity extends AppCompatActivity {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView historyList = findViewById(R.id.historyList);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        historyList.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Workouts Completed");
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.white, getTheme()));

        AppContainer container = ((IngotApplication) getApplication()).container;
        viewModel = new ViewModelProvider(this, new HistoryViewModelFactory(container))
                .get(HistoryViewModel.class);

        viewModel.getHistory().observe(this, adapter::submitList);
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Failed to load history: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        OnBackPressedDispatcher obpd = getOnBackPressedDispatcher();
        obpd.onBackPressed();
        return true;
    }
}