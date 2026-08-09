package dev.minkin.ingot.ui.home;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import dev.minkin.ingot.AppContainer;
import dev.minkin.ingot.IngotApplication;
import dev.minkin.ingot.R;
import dev.minkin.ingot.engine.model.Exercise;
import dev.minkin.ingot.engine.model.MaterializedExercise;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppContainer appContainer = ((IngotApplication) getApplication()).container;
        HomeViewModel viewModel = new ViewModelProvider(this, new HomeViewModelFactory(appContainer))
                .get(HomeViewModel.class);

        LinearLayout container = (LinearLayout) findViewById(R.id.exerciseContainer);
        viewModel.getSession().observe(this, materializedSession -> {
           container.removeAllViews();

            TextView header = new TextView(this);
            header.setText(materializedSession.getLabel());
            header.setTextSize(20);
            container.addView(header);

            for (MaterializedExercise me : materializedSession.getExercises()) {
                TextView row = new TextView(this);
                row.setPadding(0, 12, 0, 12);
                row.setText(formatRow(me));
                container.addView(row);
            }
        });
    }

    private String formatRow(MaterializedExercise me) {
        Exercise ex = me.getExercise();
        String loadText = me.getLoad() != null
                ? String.format("%s lbs", me.getLoad())
                : "—";
        return String.format("%s — %d×%s @ %s",
                ex.getName(), ex.getWorkingSets(), ex.getReps(), loadText);
    }
}