package dev.minkin.ingot.ui.program;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.ui.home.HomeActivity;
import dev.minkin.ingot.ui.program.adapters.ProgramPickerAdapter;

public class ProgramPickerActivity extends AppCompatActivity {

    private ProgramPickerViewModel viewModel;
    private ProgramPickerAdapter adapter;
    private String currentActiveId;
    private java.util.List<dev.minkin.ingot.data.repo.types.ProgramSummary> currentPrograms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Choose Program");
        }

        RecyclerView list = findViewById(R.id.programList);
        list.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProgramPickerAdapter(programId -> {
            viewModel.selectProgram(programId);
            Toast.makeText(this, "Program updated", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        list.setAdapter(adapter);

        AppContainer container = ((IngotApplication) getApplication()).container;
        viewModel = new ViewModelProvider(this, new ProgramPickerViewModelFactory(container))
                .get(ProgramPickerViewModel.class);

        viewModel.getPrograms().observe(this, programs -> {
            currentPrograms = programs;
            if (currentActiveId != null) adapter.submitList(programs, currentActiveId);
        });
        viewModel.getActiveProgramId().observe(this, activeId -> {
            currentActiveId = activeId;
            if (currentPrograms != null) adapter.submitList(currentPrograms, activeId);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}