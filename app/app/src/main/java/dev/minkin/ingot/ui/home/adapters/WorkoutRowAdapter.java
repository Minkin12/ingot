package dev.minkin.ingot.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.R;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import dev.minkin.ingot.engine.model.MaterializedExercise;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.ui.formatting.ExerciseFormatter;
import dev.minkin.ingot.ui.home.types.Status;
import dev.minkin.ingot.ui.home.types.WorkoutState;

public class WorkoutRowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_COMPACT = 0;
    private static final int TYPE_HERO = 1;
    public interface OnRowClicked {
        void onClicked(WorkoutState row);
        void onStartClicked(MaterializedSession session);
    }

    private List<WorkoutState> rows = new ArrayList<>();
    private SessionCoordinates heroCoords;
    private final OnRowClicked callback;

    public WorkoutRowAdapter(OnRowClicked callback) { this.callback = callback; }

    public void submitList(List<WorkoutState> newRows, SessionCoordinates heroCoords) {
        rows = newRows;
        this.heroCoords = heroCoords;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        WorkoutState row = rows.get(position);
        boolean isHero = row.getSession().getWeekNumber() == heroCoords.weekNumber
                && row.getSession().getDayNumber() == heroCoords.dayNumber;
        return isHero ? TYPE_HERO : TYPE_COMPACT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HERO) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hero_card, parent, false);
            return new HeroHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_row, parent, false);
        return new RowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WorkoutState row = rows.get(position);
        if (holder instanceof HeroHolder) {
            bindHero((HeroHolder) holder, row);
        } else {
            bindCompact((RowHolder) holder, row);
        }
    }

    private void bindCompact(RowHolder holder, WorkoutState row) {
        holder.name.setText(row.getSession().getLabel());
        holder.detail.setText(summarize(row.getSession()));
        holder.status.setText(statusGlyph(row.getStatus()));
        holder.itemView.setOnClickListener(v -> callback.onClicked(row));
    }

    private void bindHero(HeroHolder holder, WorkoutState row) {
        MaterializedSession s = row.getSession();
        holder.title.setText(s.getLabel());
        holder.count.setText(s.getExercises().size() + " exercises");

        holder.exerciseList.removeAllViews();
        for (MaterializedExercise me : s.getExercises()) {
            TextView line = new TextView(holder.itemView.getContext());
            line.setText(ExerciseFormatter.formatRow(me));
            holder.exerciseList.addView(line);
        }
        holder.startButton.setOnClickListener(v -> callback.onStartClicked(s));
    }

    @Override
    public int getItemCount() { return rows.size(); }

    private String statusGlyph(Status status) {
        switch (status) {
            case COMPLETED: return "✓";
            case IN_PROGRESS: return "›";
            default: return "";
        }
    }

    private String summarize(MaterializedSession session) {
        if (session.getExercises().isEmpty()) return "";
        return session.getExercises().get(0).getExercise().getName();
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        TextView name, detail, status;
        RowHolder(View v) {
            super(v);
            name = v.findViewById(R.id.rowName);
            detail = v.findViewById(R.id.rowDetail);
            status = v.findViewById(R.id.rowStatus);
        }
    }

    static class HeroHolder extends RecyclerView.ViewHolder {
        TextView title, count;
        LinearLayout exerciseList;
        Button startButton;
        HeroHolder(View v) {
            super(v);
            title = v.findViewById(R.id.heroTitle);
            count = v.findViewById(R.id.heroCount);
            exerciseList = v.findViewById(R.id.heroExerciseList);
            startButton = v.findViewById(R.id.heroStartButton);
        }
    }
}
