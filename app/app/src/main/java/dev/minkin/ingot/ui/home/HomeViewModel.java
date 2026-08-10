package dev.minkin.ingot.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.WorkoutRepository;
import dev.minkin.ingot.engine.model.Day;
import dev.minkin.ingot.engine.model.MaterializedExercise;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.engine.model.Program;
import dev.minkin.ingot.engine.model.Week;
import dev.minkin.ingot.ui.home.types.HomeUiState;
import dev.minkin.ingot.ui.home.types.Status;
import dev.minkin.ingot.ui.home.types.WorkoutState;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedWeek = new MutableLiveData<>();
    private final MediatorLiveData<HomeUiState> uiState = new MediatorLiveData<>();
    private final MutableLiveData<SessionCoordinates> focusedWorkout = new MutableLiveData<>();

    private LiveData<List<SessionCoordinates>> completedLive;
    private LiveData<List<SessionCoordinates>> startedLive;

    public HomeViewModel(ProgramRepository programRepo, WorkoutRepository workoutRepo, ExecutorService executor) {
        completedLive = workoutRepo.observeCompletedSessions();
        startedLive = workoutRepo.observeStartedSessions();

        Runnable recompute = () -> executor.execute(() -> {
            List<SessionCoordinates> completed = completedLive.getValue();
            List<SessionCoordinates> started = startedLive.getValue();
            Integer week = selectedWeek.getValue();
            if (completed == null || started == null) return;

            Program program;
            try {
                program = programRepo.getProgram();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Set<SessionCoordinates> completedSet = new HashSet<>(completed);
            Set<SessionCoordinates> startedSet = new HashSet<>(started);
            SessionCoordinates focus = focusedWorkout.getValue();
            SessionCoordinates heroCoords = (focus != null) ? focus : findUpNext(program, completedSet);

            int weekToShow = (week != null) ? week : heroCoords.weekNumber;

            List<WorkoutState> rows = new ArrayList<>();
            for (Day day : program.getWeek(weekToShow).getDays()) {
                MaterializedSession session;
                try {
                    session = programRepo.materializeSession(weekToShow, day.getDayNumber());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Map<String, String> prefill = buildPrefillMap(session, workoutRepo);
                session = programRepo.enrich(session, prefill);

                Status status = Status.deriveStatus(completedSet, startedSet, weekToShow, day.getDayNumber());
                rows.add(new WorkoutState(session, status));
            }

            uiState.postValue(new HomeUiState(weekToShow, rows, heroCoords));
        });


        uiState.addSource(completedLive, v -> recompute.run());
        uiState.addSource(startedLive, v -> recompute.run());
        uiState.addSource(selectedWeek, v -> recompute.run());
        uiState.addSource(focusedWorkout, v -> recompute.run());
    }

    public void selectWeek(int weekNumber) {
        selectedWeek.setValue(weekNumber);
        focusedWorkout.setValue(null);
    }

    public LiveData<HomeUiState> getUiState() {
        return uiState;
    }

    public void selectWorkout(SessionCoordinates coordinates) {
        focusedWorkout.setValue(coordinates);
    }
    public static SessionCoordinates findUpNext(Program program, Set<SessionCoordinates> completed) {
        for (Week week : program.getWeeks()) {
            for (Day day : week.getDays()) {
                SessionCoordinates coords = new SessionCoordinates(week.getNumber(), day.getDayNumber());
                if (!completed.contains(coords)) {
                    return coords;
                }
            }
        }
        return new SessionCoordinates();
    }
    private Map<String, String> buildPrefillMap(MaterializedSession session, WorkoutRepository workoutRepo) {
        Map<String, String> prefill = new HashMap<>();
        for (MaterializedExercise me : session.getExercises()) {
            if (me.getLoad() == null) {
                List<PerformedSetEventEntity> lastSets = workoutRepo.getLastTimeForExercise(me.getExercise().getName());
                if (!lastSets.isEmpty()) {
                    prefill.put(me.getExercise().getName(), lastSets.get(0).weightLbs);
                }
            }
        }
        return prefill;
    }
}
