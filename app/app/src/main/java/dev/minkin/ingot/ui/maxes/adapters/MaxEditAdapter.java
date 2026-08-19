package dev.minkin.ingot.ui.maxes.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.minkin.ingot.R;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.ui.maxes.types.MaxEditRow;

public class MaxEditAdapter extends RecyclerView.Adapter<MaxEditAdapter.RowHolder> {

    private List<MaxEditRow> rows = new ArrayList<>();
    private final Map<MajorLift, String> pendingValues = new HashMap<>();

    public void submitList(List<MaxEditRow> newRows) {
        this.rows = newRows;
        for (MaxEditRow row : newRows) {

            pendingValues.putIfAbsent(row.getLift(), String.valueOf(row.getCurrentValue()));
        }
        notifyDataSetChanged();
    }

    public Double getCurrentInput(MajorLift lift) {
        String text = pendingValues.get(lift);
        if (text == null) return null;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_max_edit_row, parent, false);
        return new RowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        MaxEditRow row = rows.get(position);
        MajorLift lift = row.getLift();

        holder.label.setText(lift.getDisplayName());

        if (holder.watcher != null) {
            holder.input.removeTextChangedListener(holder.watcher);
        }
        holder.input.setText(pendingValues.get(lift));

        holder.watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                pendingValues.put(lift, s.toString());
            }
        };
        holder.input.addTextChangedListener(holder.watcher);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        TextView label;
        EditText input;
        TextWatcher watcher;

        RowHolder(View v) {
            super(v);
            label = v.findViewById(R.id.liftLabel);
            input = v.findViewById(R.id.liftValueInput);
        }
    }
}
