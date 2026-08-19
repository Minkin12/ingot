package dev.minkin.ingot.ui.maxes;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.ui.InsetAwareActivity;
import dev.minkin.ingot.ui.maxes.adapters.MaxEditAdapter;
import dev.minkin.ingot.ui.maxes.types.MaxEditRow;

public class EditMaxesActivity extends InsetAwareActivity {
    private EditMaxesViewModel viewModel;
    private MaxEditAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_maxes);
        applyStatusBarInsets(R.id.rootLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Maxes");

        RecyclerView list = findViewById(R.id.maxList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaxEditAdapter();
        list.setAdapter(adapter);

        AppContainer container = ((IngotApplication) getApplication()).container;
        viewModel = new ViewModelProvider(this, new EditMaxesViewModelFactory(container))
                .get(EditMaxesViewModel.class);

        viewModel.getCurrentMaxes().observe(this, maxes -> {
            List<MaxEditRow> rows = new ArrayList<>();
            for (MajorLift lift : MajorLift.values()) {
                rows.add(new MaxEditRow(lift,  maxes.get(lift) !=  null ? maxes.get(lift).intValue() : 0));
            }
            adapter.submitList(rows);
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> {
            for (MajorLift lift : MajorLift.values()) {
                Double newValue = adapter.getCurrentInput(lift);
                if (newValue != null) {
                    viewModel.updateMax(lift, newValue);
                }
            }
            Toast.makeText(this, "Maxes updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}