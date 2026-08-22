package dev.minkin.ingot.data.db.repo;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.dao.OutboxDao;
import dev.minkin.ingot.data.db.dao.PerformedSetEventDao;
import dev.minkin.ingot.data.db.dao.WorkoutCompletedEventDao;
import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.WorkoutCompletedEventEntity;
import dev.minkin.ingot.data.repo.WorkoutRepository;

public class WorkoutRepositoryTest {

    @Mock
    private PerformedSetEventDao performedSetEventDao;
    @Mock
    private WorkoutCompletedEventDao workoutCompletedEventDao;
    @Mock
    private OutboxDao outboxDao;
    @Mock
    private ExecutorService executorService;

    private WorkoutRepository workoutRepository;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        workoutRepository = new WorkoutRepository(performedSetEventDao, workoutCompletedEventDao, executorService);

        // Mock executor to run synchronously
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).execute(any(Runnable.class));
    }

    @Test
    public void logSet_insertsPerformedSetEvent() throws JsonProcessingException {
        workoutRepository.logSet(1, 2, "Squat", 1, "100", 5, "Felt good");
        
        ArgumentCaptor<PerformedSetEventEntity> captor = ArgumentCaptor.forClass(PerformedSetEventEntity.class);
        ArgumentCaptor<OutboxEntity> captor2 = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(performedSetEventDao, times(1)).insertPerformedSetAndQueue(captor.capture(), captor2.capture());
        
        PerformedSetEventEntity entity = captor.getValue();
        assertEquals(1, entity.weekNumber);
        assertEquals(2, entity.dayNumber);
        assertEquals("Squat", entity.exerciseName);
        assertEquals(1, entity.setNumber);
        assertEquals("100", entity.weightLbs);
        assertEquals(5, entity.reps);
        assertEquals("Felt good", entity.note);
    }

    @Test
    public void logCompletedWorkout_insertsWorkoutCompletedEvent() throws JsonProcessingException {
        workoutRepository.logCompletedWorkout(1, 2, "Good session", "FULL BODY 2", List.of());
        
        ArgumentCaptor<WorkoutCompletedEventEntity> captor = ArgumentCaptor.forClass(WorkoutCompletedEventEntity.class);
        ArgumentCaptor<OutboxEntity> captor2 = ArgumentCaptor.forClass(OutboxEntity.class);

        verify(workoutCompletedEventDao, times(1)).insertWorkoutCompletedAndQueue(captor.capture(), captor2.capture());
        
        WorkoutCompletedEventEntity entity = captor.getValue();
        assertEquals(1, entity.weekNumber);
        assertEquals(2, entity.dayNumber);
        assertEquals("Good session", entity.sessionNote);
    }

    @Test
    public void observeSessionByCoordinates_callsDao() {
        LiveData<List<PerformedSetEventEntity>> liveData = new MutableLiveData<>();
        when(performedSetEventDao.observePerformedSetsForSessionCoordinates(1, 2)).thenReturn(liveData);

        LiveData<List<PerformedSetEventEntity>> result = workoutRepository.observeSessionByCoordinates(1, 2);
        
        verify(performedSetEventDao).observePerformedSetsForSessionCoordinates(1, 2);
        assertEquals(liveData, result);
    }

    @Test
    public void getLastTimeForExercise_callsDao() {
        when(performedSetEventDao.retrievePerformedSetForExercise("Squat")).thenReturn(Collections.emptyList());

        workoutRepository.getLastTimeForExercise("Squat");
        verify(performedSetEventDao).retrievePerformedSetForExercise("Squat");
    }

    @Test
    public void observeStartedSessions_callsDao() {
        workoutRepository.observeStartedSessions();
        verify(performedSetEventDao).observeStartedSessionCoordinates();
    }

    @org.junit.After
    public void tearDown() throws Exception {
        closeable.close();
    }
}
