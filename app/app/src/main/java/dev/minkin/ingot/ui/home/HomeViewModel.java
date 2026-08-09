package dev.minkin.ingot.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import dev.minkin.ingot.data.repo.ProgramRepository;
import dev.minkin.ingot.data.repo.WorkoutRepository;
import dev.minkin.ingot.engine.model.Day;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.engine.model.Program;
import dev.minkin.ingot.engine.model.Week;
import dev.minkin.ingot.ui.home.types.HomeUiState;
import dev.minkin.ingot.ui.home.types.Status;
import dev.minkin.ingot.ui.home.types.WorkoutState;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedWeek = new MutableLiveData<>();
    private final MediatorLiveData<HomeUiState> uiState = new MediatorLiveData<>();

    private LiveData<List<SessionCoordinates>> completedLive;
    private LiveData<List<SessionCoordinates>> startedLive;

    public HomeViewModel(ProgramRepository programRepo, WorkoutRepository workoutRepo, ExecutorService executor) {
        completedLive = workoutRepo.observeCompletedSessions();
        startedLive = workoutRepo.observeStartedSessions();

        Runnable recompute = () -> executor.execute(() -> {
            List<SessionCoordinates> completed = completedLive.getValue();
            List<SessionCoordinates> started = startedLive.getValue();
            Integer week = selectedWeek.getValue();
            if (completed == null || started == null) return; // not all sources ready yet

            Program program = null;
            try {
                program = programRepo.getProgram();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Set<SessionCoordinates> completedSet = new HashSet<>(completed);

            int weekToShow = (week != null) ? week : findUpNext(program, completedSet).weekNumber;

            List<WorkoutState> rows = new ArrayList<>();
            for (Day day : program.getWeek(weekToShow).getDays()) {
                MaterializedSession session = null;
                try {
                    session = programRepo.materializeSession(weekToShow, day.getDayNumber());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Status status = Status.deriveStatus(completedSet, new HashSet<>(started),
                        weekToShow, day.getDayNumber());
                rows.add(new WorkoutState(session, status));
            }

            uiState.postValue(new HomeUiState(weekToShow, rows, findUpNext(program, completedSet)));
        });

        uiState.addSource(completedLive, v -> recompute.run());
        uiState.addSource(startedLive, v -> recompute.run());
        uiState.addSource(selectedWeek, v -> recompute.run());
    }

    public void selectWeek(int weekNumber) {
        selectedWeek.setValue(weekNumber);
    }

    public LiveData<HomeUiState> getUiState() {
        return uiState;
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
        return null; //Program over, handle this state in the view
    }
}
