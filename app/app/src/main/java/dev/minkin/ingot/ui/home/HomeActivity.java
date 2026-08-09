package dev.minkin.ingot.ui.home;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.ui.home.types.HomeUiState;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView weekStrip;
    private RecyclerView workoutList;
    private WeekPillAdapter pillAdapter;
    private WorkoutRowAdapter rowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weekStrip = findViewById(R.id.weekStrip);
        workoutList = findViewById(R.id.workoutList);
        weekStrip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        workoutList.setLayoutManager(new LinearLayoutManager(this));

        AppContainer appContainer = ((IngotApplication) getApplication()).container;
        HomeViewModel viewModel = new ViewModelProvider(this, new HomeViewModelFactory(appContainer))
                .get(HomeViewModel.class);

        pillAdapter = new WeekPillAdapter(weekNumbers(1, 10), 1, week -> viewModel.selectWeek(week));
        weekStrip.setAdapter(pillAdapter);

        rowAdapter = new WorkoutRowAdapter(row -> {
            // todo launch WorkoutActivity with row's coordinates, later
        });
        workoutList.setAdapter(rowAdapter);

        viewModel.getUiState().observe(this, this::render);
    }

    private void render(HomeUiState state) {
        ((TextView) findViewById(R.id.weekLabel)).setText("Week " + state.getSelectedWeekNumber());
        pillAdapter.setSelectedWeek(state.getSelectedWeekNumber());
        rowAdapter.submitList(state.getWorkouts());
        // todo bind upNextCard's views similarly, from state.getUpNext()
    }

    private List<Integer> weekNumbers(int from, int to) {
        List<Integer> list = new ArrayList<>();
        for (int i = from; i <= to; i++) list.add(i);
        return list;
    }
}