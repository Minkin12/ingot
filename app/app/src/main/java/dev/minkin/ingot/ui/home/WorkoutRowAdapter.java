package dev.minkin.ingot.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.R;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.ui.home.types.Status;
import dev.minkin.ingot.ui.home.types.WorkoutState;

public class WorkoutRowAdapter extends RecyclerView.Adapter<WorkoutRowAdapter.RowHolder> {
    public interface OnRowClicked { void onClicked(WorkoutState row); }

    private List<WorkoutState> rows = new ArrayList<>();
    private final OnRowClicked callback;

    public WorkoutRowAdapter(OnRowClicked callback) { this.callback = callback; }

    public void submitList(List<WorkoutState> newRows) {
        rows = newRows;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_row, parent, false);
        return new RowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        WorkoutState row = rows.get(position);
        holder.name.setText(row.getSession().getLabel());
        holder.detail.setText(summarize(row.getSession()));
        holder.status.setText(statusGlyph(row.getStatus()));
        holder.itemView.setOnClickListener(v -> callback.onClicked(row));
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
}
