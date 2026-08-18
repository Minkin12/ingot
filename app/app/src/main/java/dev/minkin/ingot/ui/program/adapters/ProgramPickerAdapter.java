package dev.minkin.ingot.ui.program.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.R;
import dev.minkin.ingot.data.repo.types.ProgramSummary;

public class ProgramPickerAdapter extends RecyclerView.Adapter<ProgramPickerAdapter.RowHolder> {

    public interface OnProgramSelected {
        void onSelected(String programId);
    }

    private List<ProgramSummary> programs = new ArrayList<>();
    private String activeProgramId;
    private final OnProgramSelected callback;

    public ProgramPickerAdapter(OnProgramSelected callback) {
        this.callback = callback;
    }

    public void submitList(List<ProgramSummary> newPrograms, String activeId) {
        this.programs = newPrograms;
        this.activeProgramId = activeId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_program_row, parent, false);
        return new RowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        ProgramSummary summary = programs.get(position);
        holder.name.setText(summary.getName());
        boolean isActive = summary.getProgramId().equals(activeProgramId);
        holder.activeIndicator.setVisibility(isActive ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> callback.onSelected(summary.getProgramId()));
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        TextView name, activeIndicator;

        RowHolder(View v) {
            super(v);
            name = v.findViewById(R.id.rowProgramName);
            activeIndicator = v.findViewById(R.id.rowActiveIndicator);
        }
    }
}