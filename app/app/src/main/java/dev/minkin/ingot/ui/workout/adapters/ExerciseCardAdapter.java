package dev.minkin.ingot.ui.workout.adapters;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.minkin.ingot.R;
import dev.minkin.ingot.ui.workout.types.ExerciseUiState;
import dev.minkin.ingot.ui.workout.types.SetRowUiState;

public class ExerciseCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ACTIVE = 0;
    private static final int TYPE_COLLAPSED = 1;

    public interface Callback {
        void onExerciseTapped(int index);
        void onConfirmSet(String exerciseName, int setNumber, String weight, int reps, String note);
        void onEditWeight(String exerciseName, int setNumber, String newWeight);
        void onEditReps(String exerciseName, int setNumber, int newReps);
        void onEditNote(String exerciseName, int setNumber, String newNote);
        void onAddSet(String exerciseName);
        void onSwapExercise(String exerciseName);
    }

    private List<ExerciseUiState> exercises = new ArrayList<>();
    private int activeIndex = -1;
    private final Callback callback;

    public ExerciseCardAdapter(Callback callback) { this.callback = callback; }

    public void submitList(List<ExerciseUiState> exercises, int activeIndex) {
        this.exercises = exercises;
        this.activeIndex = activeIndex;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == activeIndex ? TYPE_ACTIVE : TYPE_COLLAPSED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ACTIVE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise_card, parent, false);
            return new ActiveHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_collapsed, parent, false);
        return new CollapsedHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExerciseUiState ex = exercises.get(position);
        if (holder instanceof ActiveHolder) {
            bindActive((ActiveHolder) holder, ex, position);
        } else {
            bindCollapsed((CollapsedHolder) holder, ex, position);
        }
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    private void bindActive(ActiveHolder holder, ExerciseUiState ex, int position) {
        holder.name.setText(ex.getExerciseName());
        holder.target.setText(ex.getTargetSummary());
        holder.lastTime.setText(ex.getLastTimeSummary() != null ? "Last time: " + ex.getLastTimeSummary() : "");

        holder.setRows.removeAllViews();
        for (SetRowUiState set : ex.getSets()) {
            holder.setRows.addView(buildSetRow(holder.itemView.getContext(), ex.getExerciseName(), set));
        }

        holder.addSetButton.setOnClickListener(v -> callback.onAddSet(ex.getExerciseName()));
        holder.swapButton.setOnClickListener(v -> callback.onSwapExercise(ex.getExerciseName()));
    }

    private void bindCollapsed(CollapsedHolder holder, ExerciseUiState ex, int position) {
        holder.name.setText(ex.getExerciseName());
        holder.target.setText(ex.getTargetSummary());
        holder.status.setText(allSetsConfirmed(ex) ? "✓" : "");
        holder.itemView.setOnClickListener(v -> callback.onExerciseTapped(position));
    }

    private boolean allSetsConfirmed(ExerciseUiState ex) {
        return ex.getSets().stream().allMatch(SetRowUiState::isConfirmed);
    }

    private View buildSetRow(android.content.Context context, String exerciseName, SetRowUiState set) {
        View row = LayoutInflater.from(context).inflate(R.layout.item_set_row, null, false);

        TextView numberView = row.findViewById(R.id.setNumber);
        TextView weightView = row.findViewById(R.id.setWeight);
        TextView repsView = row.findViewById(R.id.setReps);
        TextView noteView = row.findViewById(R.id.setNote);
        ImageButton noteButton = row.findViewById(R.id.noteButton);
        ImageButton confirmButton = row.findViewById(R.id.confirmButton);

        numberView.setText(String.valueOf(set.getSetNumber()));
        weightView.setText(set.getWeight());
        repsView.setText(String.valueOf(set.getReps()));
        if (set.getNote() != null && !set.getNote().isEmpty()) {
            noteView.setVisibility(View.VISIBLE);
            noteView.setText(set.getNote());
        } else {
            noteView.setVisibility(View.GONE);
        }

        row.setBackgroundResource(set.isConfirmed()
                ? R.drawable.bg_set_row_confirmed
                : R.drawable.bg_set_row);

        confirmButton.setImageResource(set.isConfirmed()
                ? R.drawable.ic_radio_button_checked
                : R.drawable.ic_radio_button_outline);

        weightView.setOnClickListener(v -> promptForText(context, "Weight (lbs)", set.getWeight(),
                value -> callback.onEditWeight(exerciseName, set.getSetNumber(), value)));

        repsView.setOnClickListener(v -> promptForNumber(context, "Reps", set.getReps(),
                value -> callback.onEditReps(exerciseName, set.getSetNumber(), value)));

        noteButton.setOnClickListener(v -> promptForText(context, "Note for set " + set.getSetNumber(),
                set.getNote(), text -> callback.onEditNote(exerciseName, set.getSetNumber(), text)));

        confirmButton.setOnClickListener(v -> callback.onConfirmSet(
                exerciseName, set.getSetNumber(), set.getWeight(), set.getReps(), set.getNote()));

        return row;
    }

    private interface NumberCallback { void onValue(int value); }
    private interface TextCallback { void onValue(String value); }

    private void promptForNumber(android.content.Context context, String title, int current,
                                 NumberCallback cb) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(current));
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    try {
                        cb.onValue(Integer.parseInt(String.valueOf(input.getText())));
                    } catch (NumberFormatException ignored) { }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptForText(android.content.Context context, String title, String current, TextCallback cb) {
        EditText input = new EditText(context);
        if (current != null) input.setText(current);
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("OK", (d, w) -> cb.onValue(input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    static class ActiveHolder extends RecyclerView.ViewHolder {
        TextView name, target, lastTime;
        LinearLayout setRows;
        View addSetButton, swapButton;
        ActiveHolder(View v) {
            super(v);
            name = v.findViewById(R.id.exerciseName);
            target = v.findViewById(R.id.exerciseTarget);
            lastTime = v.findViewById(R.id.exerciseLastTime);
            setRows = v.findViewById(R.id.setRowsContainer);
            addSetButton = v.findViewById(R.id.addSetButton);
            swapButton = v.findViewById(R.id.swapExerciseButton);
        }
    }

    static class CollapsedHolder extends RecyclerView.ViewHolder {
        TextView name, target, status;
        CollapsedHolder(View v) {
            super(v);
            name = v.findViewById(R.id.exerciseName);
            target = v.findViewById(R.id.exerciseTarget);
            status = v.findViewById(R.id.exerciseStatus);
        }
    }
}