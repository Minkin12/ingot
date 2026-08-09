package dev.minkin.ingot.data.db.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.res.AssetManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;
import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;
import dev.minkin.ingot.data.repo.ProgramTemplateDao;
import dev.minkin.ingot.data.repo.TrainingMaxDao;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.engine.model.Maxes;

public class ProgramRepositoryTest {

    @Mock
    private ProgramTemplateDao programTemplateDao;
    @Mock
    private TrainingMaxDao trainingMaxDao;
    @Mock
    private ExecutorService executorService;
    @Mock
    private AssetManager assetManager;

    private ProgramRepository programRepository;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        programRepository = new ProgramRepository(programTemplateDao, trainingMaxDao, executorService, assetManager);

        // Mock executor to run synchronously
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).execute(any(Runnable.class));
    }

    @Test
    public void ensureSeeded_alreadySeeded_doesNothing() throws IOException {
        when(programTemplateDao.selectProgramTemplate()).thenReturn(new ProgramTemplateEntity());

        programRepository.ensureSeeded();

        verify(assetManager, never()).open(anyString());
        verify(programTemplateDao, never()).insertProgram(any());
        verify(trainingMaxDao, never()).insertMax(any());
    }

    @Test
    public void ensureSeeded_notSeeded_seedsData() throws IOException {
        String testJson = "{\"name\": \"Test Program\", \"oneRepMaxes\": {\"squat\": 100, \"bench\": 80, \"deadlift\": 120, \"hip_thrust\": 150}, \"weeks\": []}";
        when(programTemplateDao.selectProgramTemplate()).thenReturn(null);
        InputStream inputStream = new ByteArrayInputStream(testJson.getBytes(StandardCharsets.UTF_8));
        when(assetManager.open("program.json")).thenReturn(inputStream);

        programRepository.ensureSeeded();

        verify(programTemplateDao, times(1)).insertProgram(any(ProgramTemplateEntity.class));
        
        ArgumentCaptor<TrainingMaxEntity> captor = ArgumentCaptor.forClass(TrainingMaxEntity.class);
        verify(trainingMaxDao, times(MajorLift.values().length)).insertMax(captor.capture());
        
        List<TrainingMaxEntity> insertedMaxes = captor.getAllValues();
        assertEquals(4, insertedMaxes.size());
        
        // Verify squat max
        TrainingMaxEntity squatMax = insertedMaxes.stream()
                .filter(m -> m.lift.equals(MajorLift.SQUAT.getJsonName()))
                .findFirst().orElse(null);
        assertNotNull(squatMax);
        assertEquals(100.0, squatMax.valueLbs, 0.01);

        // Verify bench max
        TrainingMaxEntity benchMax = insertedMaxes.stream()
                .filter(m -> m.lift.equals(MajorLift.BENCH.getJsonName()))
                .findFirst().orElse(null);
        assertNotNull(benchMax);
        assertEquals(80.0, benchMax.valueLbs, 0.01);
    }

    @Test
    public void getCurrentMaxes_returnsMaxes() {
        TrainingMaxEntity squat = new TrainingMaxEntity();
        squat.lift = "squat";
        squat.valueLbs = 100.0;
        
        TrainingMaxEntity bench = new TrainingMaxEntity();
        bench.lift = "bench";
        bench.valueLbs = 80.0;
        
        TrainingMaxEntity deadlift = new TrainingMaxEntity();
        deadlift.lift = "deadlift";
        deadlift.valueLbs = 120.0;
        
        TrainingMaxEntity hipThrust = new TrainingMaxEntity();
        hipThrust.lift = "hip_thrust";
        hipThrust.valueLbs = 150.0;

        when(trainingMaxDao.selectCurrentMaxes()).thenReturn(List.of(squat, bench, deadlift, hipThrust));

        Maxes maxes = programRepository.getCurrentMaxes();

        assertNotNull(maxes);
        assertEquals(100.0, maxes.getMaxWeight(MajorLift.SQUAT), 0.01);
        assertEquals(80.0, maxes.getMaxWeight(MajorLift.BENCH), 0.01);
    }

    @Test(expected = IllegalStateException.class)
    public void materializeSession_notSeeded_throwsException() throws IOException {
        when(programTemplateDao.selectProgramTemplate()).thenReturn(null);
        programRepository.materializeSession(1, 1);
    }

    @Test
    public void recordNewMax_insertsMax() {
        programRepository.recordNewMax(MajorLift.SQUAT, 200.0);
        verify(trainingMaxDao, times(1)).insertMax(any(TrainingMaxEntity.class));
    }

    @org.junit.After
    public void tearDown() throws Exception {
        closeable.close();
    }
}
