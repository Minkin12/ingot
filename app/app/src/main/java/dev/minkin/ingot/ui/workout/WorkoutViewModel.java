package dev.minkin.ingot.ui.workout;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.WorkoutRepository;
import dev.minkin.ingot.engine.Rounding;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.engine.model.MaterializedExercise;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.ui.formatting.ExerciseFormatter;
import dev.minkin.ingot.ui.workout.types.ExerciseUiState;
import dev.minkin.ingot.ui.workout.types.MaxSuggestion;
import dev.minkin.ingot.ui.workout.types.SetRowUiState;
import dev.minkin.ingot.ui.workout.types.WorkoutUiState;

public class WorkoutViewModel extends ViewModel {
    private final ProgramRepository programRepo;
    private final WorkoutRepository workoutRepo;
    private final ExecutorService executor;
    private final int weekNumber;
    private final int dayNumber;
    private MaterializedSession enrichedSession;
    private int activeExerciseIndex = 0;
    private String sessionNote = "";
    private String workoutLabel = "";

    private final Map<String, String> pendingWeights = new HashMap<>();
    private final Map<String, Integer> pendingReps = new HashMap<>();
    private final Map<String, String> pendingNotes = new HashMap<>();
    private final Map<String, Integer> extraSetCounts = new HashMap<>();

    private final MediatorLiveData<WorkoutUiState> uiState = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> finishComplete = new MutableLiveData<>();
    private final LiveData<List<PerformedSetEventEntity>> liveSets;
    private final MutableLiveData<MaxSuggestion> maxSuggestion = new MutableLiveData<>();


    public WorkoutViewModel(ProgramRepository programRepo, WorkoutRepository workoutRepo,
                            ExecutorService executor, int weekNumber, int dayNumber) {
        this.programRepo = programRepo;
        this.workoutRepo = workoutRepo;
        this.executor = executor;
        this.weekNumber = weekNumber;
        this.dayNumber = dayNumber;

        liveSets = workoutRepo.observeSessionByCoordinates(weekNumber, dayNumber);

        uiState.addSource(liveSets, sets -> recompute(sets));
        loadEnrichedSession();
    }

    private void loadEnrichedSession() {
        executor.execute(() -> {
            try {
                String programId = programRepo.getActiveProgramId();
                MaterializedSession session = programRepo.materializeSession(programId, weekNumber, dayNumber);
                Map<String, String> prefill = buildPrefillMap(session);
                enrichedSession = programRepo.enrich(session, prefill);
                workoutLabel = enrichedSession.getLabel();
                recompute(liveSets.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Map<String, String> buildPrefillMap(MaterializedSession session) {
        Map<String, String> prefill = new HashMap<>();
        for (MaterializedExercise me : session.getExercises()) {
            if (me.getLoad() == null) {
                List<PerformedSetEventEntity> lastSets =
                        workoutRepo.getLastTimeForExercise(me.getExercise().getName());
                if (!lastSets.isEmpty()) {
                    prefill.put(me.getExercise().getName(), lastSets.get(0).weightLbs);
                }
            }
        }
        return prefill;
    }

    private void recompute(List<PerformedSetEventEntity> performedSets) {
        if (enrichedSession == null || performedSets == null) return;
        executor.execute(() -> {
            List<ExerciseUiState> exerciseStates = new ArrayList<>();

            for (MaterializedExercise me : enrichedSession.getExercises()) {
                String name = me.getExercise().getName();
                int prescribedSets = me.getExercise().getWorkingSets();
                int extra = extraSetCounts.getOrDefault(name, 0);
                int totalSets = prescribedSets + extra;

                List<SetRowUiState> rows = new ArrayList<>();
                String runningPrefillWeight = (me.getLoad() != null) ? me.getLoad() : "";
                int runningPrefillReps = parseRepsFallback(me.getExercise().getReps());

                for (int setNumber = 1; setNumber <= totalSets; setNumber++) {
                    PerformedSetEventEntity performed = findPerformed(performedSets, name, setNumber);
                    String key = name + "#" + setNumber;

                    boolean hasPendingEdit = pendingWeights.containsKey(key) || pendingReps.containsKey(key) || pendingNotes.containsKey(key);

                    if (performed != null && !hasPendingEdit) {
                        rows.add(new SetRowUiState(setNumber, performed.weightLbs, performed.reps,
                                performed.note, true));
                        runningPrefillWeight = performed.weightLbs;
                        runningPrefillReps = performed.reps;

                    } else {
                        String weight = pendingWeights.getOrDefault(key,
                                performed != null ? performed.weightLbs : runningPrefillWeight);
                        int reps = pendingReps.getOrDefault(key,
                                performed != null ? performed.reps : runningPrefillReps);
                        String note = pendingNotes.getOrDefault(key,
                                performed != null ? performed.note : null);

                        rows.add(new SetRowUiState(setNumber, weight, reps, note, false));

                        runningPrefillWeight = weight;
                        runningPrefillReps = reps;
                    }
                }

                String lastTime = summarizeLastTime(name);
                exerciseStates.add(new ExerciseUiState(
                        name, ExerciseFormatter.formatRow(me), lastTime, rows));
            }

            uiState.postValue(new WorkoutUiState(
                    enrichedSession.getLabel(), exerciseStates, activeExerciseIndex, sessionNote));
        });
    }

    private PerformedSetEventEntity findPerformed(List<PerformedSetEventEntity> sets,
                                                  String exerciseName, int setNumber) {
        for (PerformedSetEventEntity e : sets) {
            if (e.exerciseName.equals(exerciseName) && e.setNumber == setNumber) return e;
        }
        return null;
    }

    private int parseRepsFallback(String reps) {
        try {
            return Integer.parseInt(reps.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String summarizeLastTime(String exerciseName) {
        List<PerformedSetEventEntity> last = workoutRepo.getLastTimeForExercise(exerciseName);
        if (last.isEmpty()) return null;
        StringBuilder reps = new StringBuilder();
        for (PerformedSetEventEntity e : last) {
            if (reps.length() > 0) reps.append(", ");
            reps.append(e.reps);
        }
        return last.get(0).weightLbs + " × " + reps;
    }

    public void setActiveExercise(int index) {
        activeExerciseIndex = index;
        recompute(liveSets.getValue());
    }

    public void confirmSet(String exerciseName, int setNumber, String weight, int reps, String note) {
        executor.execute(() -> {
            try {
                workoutRepo.logSet(weekNumber, dayNumber, exerciseName, setNumber, weight, reps, note);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            checkForPotentialMaxIncrease(exerciseName, weight, reps);
        });

        String key = exerciseName + "#" + setNumber;
        pendingWeights.remove(key);
        pendingReps.remove(key);
        pendingNotes.remove(key);
    }

    private void checkForPotentialMaxIncrease(String exerciseName, String weight, int reps) {
        if (enrichedSession == null || reps > 8) return;

        MaterializedExercise me = findExerciseByName(enrichedSession, exerciseName);
        if (me == null || !me.getExercise().getIsMaxTracking()) return;

        String sourceLiftStr = me.getExercise().getSourceLift();
        if (sourceLiftStr == null) return;

        double weightLbs;
        try {
            weightLbs = Double.parseDouble(weight);
        } catch (NumberFormatException e) {
            return;
        }
        //Epley formula rounded to nearest 5
        double estimated1RM = Rounding.toNearestFive(weightLbs * (1 + reps / 30.0));

        MajorLift lift = MajorLift.fromJson(sourceLiftStr);
        double currentMax = programRepo.getCurrentMaxes().getMaxWeight(lift);

        if (estimated1RM > currentMax) {
            maxSuggestion.postValue(new MaxSuggestion(lift, estimated1RM, currentMax));
        }
    }

    private MaterializedExercise findExerciseByName(MaterializedSession session, String name) {
        for (MaterializedExercise me : session.getExercises()) {
            if (me.getExercise().getName().equals(name)) {
                return me;
            }
        }
        return null;
    }

    public void confirmMaxSuggestion(MaxSuggestion suggestion) {
        executor.execute(() -> {
            try {
                programRepo.recordNewMax(suggestion.getLift(), suggestion.getEstimated1RM());
            } catch (JsonProcessingException e) {
                Log.e("WorkoutViewModel", "Failed to record new max", e);
            }
        });
        maxSuggestion.postValue(null);
    }

    public void updatePendingWeight(String exerciseName, int setNumber, String weight) {
        pendingWeights.put(exerciseName + "#" + setNumber, weight);
        recompute(liveSets.getValue());
    }

    public void updatePendingReps(String exerciseName, int setNumber, int reps) {
        pendingReps.put(exerciseName + "#" + setNumber, reps);
        recompute(liveSets.getValue());
    }

    public void setNoteForSet(String exerciseName, int setNumber, String note) {
        pendingNotes.put(exerciseName + "#" + setNumber, note);
        recompute(liveSets.getValue());
    }

    public void addExtraSet(String exerciseName) {
        extraSetCounts.merge(exerciseName, 1, Integer::sum);
        recompute(liveSets.getValue());
    }

    public void finishWorkout(String note) {
        this.sessionNote = note;

        executor.execute(() -> {
            try {
                workoutRepo.logCompletedWorkout(weekNumber, dayNumber, note, workoutLabel);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            finishComplete.postValue(true);
        });
    }

    public LiveData<Boolean> getFinishComplete() {
        return finishComplete;
    }

    public LiveData<WorkoutUiState> getUiState() {
        return uiState;
    }

    public LiveData<MaxSuggestion> getMaxSuggestion() {
        return maxSuggestion;
    }

    public void dismissMaxSuggestion() {
        maxSuggestion.postValue(null);
    }
}