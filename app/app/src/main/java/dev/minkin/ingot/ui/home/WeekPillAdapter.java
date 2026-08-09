package dev.minkin.ingot.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.minkin.ingot.R;

public class WeekPillAdapter extends RecyclerView.Adapter<WeekPillAdapter.PillHolder> {
    public interface OnWeekSelected { void onSelected(int weekNumber); }

    private final List<Integer> weekNumbers;
    private int selectedWeek;
    private final OnWeekSelected callback;

    public WeekPillAdapter(List<Integer> weekNumbers, int selectedWeek, OnWeekSelected callback) {
        this.weekNumbers = weekNumbers;
        this.selectedWeek = selectedWeek;
        this.callback = callback;
    }

    public void setSelectedWeek(int weekNumber) {
        selectedWeek = weekNumber;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PillHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week_pill, parent, false);
        return new PillHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PillHolder holder, int position) {
        int week = weekNumbers.get(position);
        holder.label.setText("Wk " + week);
        holder.label.setSelected(week == selectedWeek); // drive a selector state if you add one
        holder.label.setOnClickListener(v -> callback.onSelected(week));
    }

    @Override
    public int getItemCount() { return weekNumbers.size(); }

    static class PillHolder extends RecyclerView.ViewHolder {
        TextView label;
        PillHolder(View v) { super(v); label = v.findViewById(R.id.pillLabel); }
    }
}
