package dev.minkin.ingot.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import dev.minkin.ingot.data.remote.IngotApi;
import dev.minkin.ingot.data.remote.types.SessionHistoryEntry;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.ui.InsetAwareActivity;
import dev.minkin.ingot.ui.history.HistoryActivity;
import dev.minkin.ingot.ui.home.adapters.WeekPillAdapter;
import dev.minkin.ingot.ui.home.adapters.WorkoutRowAdapter;
import dev.minkin.ingot.ui.home.types.HomeUiState;
import dev.minkin.ingot.ui.home.types.WorkoutState;
import dev.minkin.ingot.ui.maxes.EditMaxesActivity;
import dev.minkin.ingot.ui.program.ProgramPickerActivity;
import dev.minkin.ingot.ui.workout.WorkoutActivity;

public class HomeActivity extends InsetAwareActivity {
    private RecyclerView weekStrip;
    private RecyclerView workoutList;
    private WeekPillAdapter pillAdapter;
    private WorkoutRowAdapter rowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyStatusBarInsets(R.id.rootLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weekStrip = findViewById(R.id.weekStrip);
        workoutList = findViewById(R.id.workoutList);
        weekStrip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        workoutList.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.viewHistoryButton).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));

        AppContainer appContainer = ((IngotApplication) getApplication()).container;
        HomeViewModel viewModel = new ViewModelProvider(this, new HomeViewModelFactory(appContainer))
                .get(HomeViewModel.class);

        pillAdapter = new WeekPillAdapter(weekNumbers(1, 10), 1, week -> viewModel.selectWeek(week));
        weekStrip.setAdapter(pillAdapter);

        rowAdapter = new WorkoutRowAdapter(new WorkoutRowAdapter.OnRowClicked() {
            @Override
            public void onClicked(WorkoutState row) {
                MaterializedSession s = row.getSession();
                viewModel.selectWorkout(new SessionCoordinates(s.getWeekNumber(), s.getDayNumber()));
            }

            @Override
            public void onStartClicked(MaterializedSession session) {
                Intent intent = new Intent(HomeActivity.this, WorkoutActivity.class);
                intent.putExtra("weekNumber", session.getWeekNumber());
                intent.putExtra("dayNumber", session.getDayNumber());
                startActivity(intent);
            }
        });
        workoutList.setAdapter(rowAdapter);

        findViewById(R.id.viewHistoryButton).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));


        viewModel.getUiState().observe(this, this::render);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose_program) {
            startActivity(new Intent(this, ProgramPickerActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_edit_maxes) {
            startActivity(new Intent(this, EditMaxesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void render(HomeUiState state) {
        ((TextView) findViewById(R.id.weekLabel)).setText("Week " + state.getSelectedWeekNumber());
        pillAdapter.setSelectedWeek(state.getSelectedWeekNumber());
        rowAdapter.submitList(state.getWorkouts(), state.getHeroCoords());
    }

    private List<Integer> weekNumbers(int from, int to) {
        List<Integer> list = new ArrayList<>();
        for (int i = from; i <= to; i++) list.add(i);
        return list;
    }
}