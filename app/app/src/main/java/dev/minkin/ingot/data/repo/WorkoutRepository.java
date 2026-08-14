package dev.minkin.ingot.data.repo;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import dev.minkin.ingot.data.db.dao.PerformedSetEventDao;
import dev.minkin.ingot.data.db.dao.WorkoutCompletedEventDao;

public class WorkoutRepository {
    private PerformedSetEventDao performedSetEventDao;
    private WorkoutCompletedEventDao workoutCompletedEventDao;
    private ExecutorService executor;

    public WorkoutRepository(PerformedSetEventDao performedSetEventDao, WorkoutCompletedEventDao workoutCompletedEventDao, ExecutorService executor){
        this.performedSetEventDao = performedSetEventDao;
        this.workoutCompletedEventDao = workoutCompletedEventDao;
        this.executor = executor;
    }

    public void logSet(int weekNumber, int dayNumber, String exercise, int setNumber, String weight, int reps, String note){
        PerformedSetEventEntity performedSetEventEntity = new PerformedSetEventEntity();
        performedSetEventEntity.eventId = UUID.randomUUID().toString();
        performedSetEventEntity.weekNumber = weekNumber;
        performedSetEventEntity.dayNumber = dayNumber;
        performedSetEventEntity.exerciseName = exercise;
        performedSetEventEntity.setNumber = setNumber;
        performedSetEventEntity.weightLbs = weight;
        performedSetEventEntity.reps = reps;
        performedSetEventEntity.note = note;
        performedSetEventEntity.loggedAt = System.currentTimeMillis();

        executor.execute(() -> performedSetEventDao.insertPerformedSetEvent(performedSetEventEntity));
    }

    public void logCompletedWorkout(int weekNumber, int dayNumber, String sessionNote, String workoutLabel){
        WorkoutCompletedEventEntity workoutCompletedEventEntity = new WorkoutCompletedEventEntity();
        workoutCompletedEventEntity.eventId = UUID.randomUUID().toString();
        workoutCompletedEventEntity.weekNumber = weekNumber;
        workoutCompletedEventEntity.dayNumber = dayNumber;
        workoutCompletedEventEntity.sessionNote = sessionNote;
        workoutCompletedEventEntity.workoutLabel = workoutLabel;
        workoutCompletedEventEntity.completedAt = System.currentTimeMillis();

        executor.execute(() -> workoutCompletedEventDao.insertWorkoutCompletedEvent(workoutCompletedEventEntity));
    }

    //TODO probably want to use a model that works best for the viewmodel, just entity for now
    public LiveData<List<PerformedSetEventEntity>> observeSessionByCoordinates(int weekNumber, int dayNumber){
        return performedSetEventDao.observePerformedSetsForSessionCoordinates(weekNumber,dayNumber);
    }

    public List<PerformedSetEventEntity> getLastTimeForExercise(String exerciseName){
        return performedSetEventDao.retrievePerformedSetForExercise(exerciseName);
    }

    public LiveData<List<SessionCoordinates>> observeStartedSessions(){
        return performedSetEventDao.observeStartedSessionCoordinates();
    }

    public LiveData<List<SessionCoordinates>> observeCompletedSessions(){
        return workoutCompletedEventDao.observeCompletedSessionCoordinates();
    }
}
