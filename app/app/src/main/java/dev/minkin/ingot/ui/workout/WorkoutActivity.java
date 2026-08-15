package dev.minkin.ingot.ui.workout;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.ui.workout.adapters.ExerciseCardAdapter;
import dev.minkin.ingot.ui.workout.types.WorkoutUiState;

public class WorkoutActivity extends AppCompatActivity {

    private WorkoutViewModel viewModel;
    private ExerciseCardAdapter adapter;

    private TextView workoutTitle;
    private RecyclerView exerciseList;
    private EditText sessionNoteInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        workoutTitle = findViewById(R.id.workoutTitle);
        exerciseList = findViewById(R.id.exerciseList);
        sessionNoteInput = findViewById(R.id.sessionNoteInput);

        int weekNumber = getIntent().getIntExtra("weekNumber", -1);
        int dayNumber = getIntent().getIntExtra("dayNumber", -1);

        AppContainer container = ((IngotApplication) getApplication()).container;
        viewModel = new ViewModelProvider(this,
                new WorkoutViewModelFactory(container, weekNumber, dayNumber))
                .get(WorkoutViewModel.class);

        setupExerciseList();

        viewModel.getFinishComplete().observe(this, done -> {
            if (done != null && done) {
                finish();
            }
        });

        findViewById(R.id.finishButton).setOnClickListener(v -> {
            String note = sessionNoteInput.getText().toString();
            viewModel.finishWorkout(note);
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.white, getTheme()));

        viewModel.getUiState().observe(this, this::render);
    }

    private void setupExerciseList() {
        adapter = new ExerciseCardAdapter(new ExerciseCardAdapter.Callback() {
            @Override
            public void onExerciseTapped(int index) {
                viewModel.setActiveExercise(index);
            }

            @Override
            public void onConfirmSet(String exerciseName, int setNumber, String weight, int reps, String note) {
                viewModel.confirmSet(exerciseName, setNumber, weight, reps, note);
            }

            @Override
            public void onEditWeight(String exerciseName, int setNumber, String newWeight) {
                viewModel.updatePendingWeight(exerciseName, setNumber, newWeight);
            }

            @Override
            public void onEditReps(String exerciseName, int setNumber, int newReps) {
                viewModel.updatePendingReps(exerciseName, setNumber, newReps);
            }

            @Override
            public void onEditNote(String exerciseName, int setNumber, String newNote) {
                viewModel.setNoteForSet(exerciseName, setNumber, newNote);
            }

            @Override
            public void onAddSet(String exerciseName) {
                viewModel.addExtraSet(exerciseName);
            }

            @Override
            public void onSwapExercise(String exerciseName) {
                // todo
            }
        });

        exerciseList.setLayoutManager(new LinearLayoutManager(this));
        exerciseList.setAdapter(adapter);
    }

    private void render(WorkoutUiState state) {
//        workoutTitle.setText(state.getWorkoutLabel());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(state.getWorkoutLabel());
        }
        adapter.submitList(state.getExercises(), state.getActiveExerciseIndex());
    }
    @Override
    public boolean onSupportNavigateUp() {
        OnBackPressedDispatcher obpd = getOnBackPressedDispatcher();
        obpd.onBackPressed();
        return true;
    }
}