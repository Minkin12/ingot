package dev.minkin.ingot.data.repo;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.EventType;
import dev.minkin.ingot.data.db.dao.PerformedSetEventDao;
import dev.minkin.ingot.data.db.dao.WorkoutCompletedEventDao;
import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.db.entity.types.SessionCoordinates;
import dev.minkin.ingot.data.repo.types.TestResult;
import dev.minkin.ingot.data.repo.types.WorkoutCompletedPayload;

public class WorkoutRepository {
    private PerformedSetEventDao performedSetEventDao;
    private WorkoutCompletedEventDao workoutCompletedEventDao;
    private ExecutorService executor;
    private ObjectMapper objectMapper = new ObjectMapper();

    public WorkoutRepository(PerformedSetEventDao performedSetEventDao, WorkoutCompletedEventDao workoutCompletedEventDao, ExecutorService executor){
        this.performedSetEventDao = performedSetEventDao;
        this.workoutCompletedEventDao = workoutCompletedEventDao;
        this.executor = executor;
    }

    public void logSet(int weekNumber, int dayNumber, String exercise, int setNumber, String weight, int reps, String note) throws JsonProcessingException {
        String eventId = UUID.randomUUID().toString();
        long currentTimeMillis = System.currentTimeMillis();

        PerformedSetEventEntity performedSetEventEntity = new PerformedSetEventEntity();
        performedSetEventEntity.eventId = eventId;
        performedSetEventEntity.weekNumber = weekNumber;
        performedSetEventEntity.dayNumber = dayNumber;
        performedSetEventEntity.exerciseName = exercise;
        performedSetEventEntity.setNumber = setNumber;
        performedSetEventEntity.weightLbs = weight;
        performedSetEventEntity.reps = reps;
        performedSetEventEntity.note = note;
        performedSetEventEntity.loggedAt = currentTimeMillis;

        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.eventId = eventId;
        outboxEntity.eventType = EventType.PERFORMED_SET.getJsonName();
        outboxEntity.payload = objectMapper.writeValueAsString(performedSetEventEntity);
        outboxEntity.createdAt = currentTimeMillis;

        executor.execute(() -> {
           performedSetEventDao.insertPerformedSetAndQueue(performedSetEventEntity, outboxEntity);
        });
    }

    public void logCompletedWorkout(int weekNumber, int dayNumber, String sessionNote, String workoutLabel, List<TestResult> testResults) throws JsonProcessingException {
        String eventId = UUID.randomUUID().toString();
        long currentTimeMillis = System.currentTimeMillis();;

        WorkoutCompletedEventEntity workoutCompletedEventEntity = new WorkoutCompletedEventEntity();
        workoutCompletedEventEntity.eventId = eventId;
        workoutCompletedEventEntity.weekNumber = weekNumber;
        workoutCompletedEventEntity.dayNumber = dayNumber;
        workoutCompletedEventEntity.sessionNote = sessionNote;
        workoutCompletedEventEntity.workoutLabel = workoutLabel;
        workoutCompletedEventEntity.completedAt = currentTimeMillis;

        WorkoutCompletedPayload payload = new WorkoutCompletedPayload();
        payload.setWeekNumber(weekNumber);
        payload.setDayNumber(dayNumber);
        payload.setSessionNote(sessionNote);
        payload.setWorkoutLabel(workoutLabel);
        payload.setTestResults(testResults);

        Log.d("Workout Repository", objectMapper.writeValueAsString(payload));
        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.eventId = eventId;
        outboxEntity.eventType = EventType.WORKOUT_COMPLETED.getJsonName();
        outboxEntity.payload = objectMapper.writeValueAsString(payload);
        outboxEntity.createdAt = currentTimeMillis;

        executor.execute(() -> {
            workoutCompletedEventDao.insertWorkoutCompletedAndQueue(workoutCompletedEventEntity, outboxEntity);
        });
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
