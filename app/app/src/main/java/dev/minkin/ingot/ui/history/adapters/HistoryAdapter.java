package dev.minkin.ingot.ui.history.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.R;
import dev.minkin.ingot.data.remote.types.SessionHistoryEntry;
import dev.minkin.ingot.ui.formatting.DateFormatter;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.RowHolder> {
    private List<SessionHistoryEntry> entries = new ArrayList<>();

    public void submitList(List<SessionHistoryEntry> newEntries) {
        entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_row, parent, false);
        return new RowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        SessionHistoryEntry entry = entries.get(position);
        String label = entry.getWorkoutLabel() != null ? entry.getWorkoutLabel()
                : "Week " + entry.getWeekNumber() + ", Day " + entry.getDayNumber();
        holder.label.setText(label);
        holder.note.setText(entry.getSessionNote() != null ? entry.getSessionNote() : "");
        holder.date.setText(DateFormatter.formatCompletedAt(entry.getCompletedAt()));
    }

    @Override
    public int getItemCount() { return entries.size(); }

    static class RowHolder extends RecyclerView.ViewHolder {
        TextView label, note, date;
        RowHolder(View v) {
            super(v);
            label = v.findViewById(R.id.rowLabel);
            note = v.findViewById(R.id.rowNote);
            date = v.findViewById(R.id.rowDate);
        }
    }
}