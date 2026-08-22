package dev.minkin.ingot.data.repo;

import android.content.res.AssetManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.EventType;
import dev.minkin.ingot.data.db.dao.AppSettingsDao;
import dev.minkin.ingot.data.db.dao.ProgramTemplateDao;
import dev.minkin.ingot.data.db.dao.TrainingMaxDao;
import dev.minkin.ingot.data.db.entity.AppSettingsEntity;
import dev.minkin.ingot.data.db.entity.OutboxEntity;
import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;
import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;
import dev.minkin.ingot.data.repo.types.ProgramSummary;
import dev.minkin.ingot.engine.Materializer;
import dev.minkin.ingot.engine.ProgramLoader;
import dev.minkin.ingot.engine.model.MajorLift;
import dev.minkin.ingot.engine.model.MaterializedExercise;
import dev.minkin.ingot.engine.model.MaterializedSession;
import dev.minkin.ingot.engine.model.Maxes;
import dev.minkin.ingot.engine.model.Program;

public class ProgramRepository {
    private ProgramTemplateDao programTemplateDao;
    private TrainingMaxDao trainingMaxDao;
    private AppSettingsDao appSettingsDao;
    private ExecutorService executor;
    private AssetManager assetManager;
    private final Map<String, Program> programCache = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public ProgramRepository(ProgramTemplateDao programTemplateDao, TrainingMaxDao trainingMaxDao, AppSettingsDao appSettingsDao, ExecutorService executor, AssetManager assetManager) {
        this.programTemplateDao = programTemplateDao;
        this.trainingMaxDao = trainingMaxDao;
        this.appSettingsDao = appSettingsDao;
        this.executor = executor;
        this.assetManager = assetManager;
    }


    public void ensureSeeded() throws IOException {
        for (String filename : assetManager.list("programs")) {
            String programId = filename.replace(".json", "");
            if (programTemplateDao.selectProgramTemplate(programId) != null) {
                continue; // this program already seeded
            }
            seedOneProgram(programId, "programs/" + filename);
        }
        seedAppSettings();
        ensureMaxesSeeded(); // todo this seeds maxes from a hardcoded source, later ask user on startup
    }

    public Maxes getCurrentMaxes() {
        List<TrainingMaxEntity> rows = trainingMaxDao.selectCurrentMaxes();

        Map<String, Double> byJsonName = new HashMap<>();
        for (TrainingMaxEntity tme : rows) {
            byJsonName.put(tme.lift, tme.valueLbs);
        }
        return Maxes.fromJsonMap(byJsonName);
    }

    public MaterializedSession materializeSession(String programId, int weekNumber, int dayNumber) throws IOException {
        return Materializer.materialize(getProgram(programId), getCurrentMaxes(), weekNumber, dayNumber);
    }

    public MaterializedSession enrich(MaterializedSession session,
                                      Map<String, String> lastWeightByExercise) {
        List<MaterializedExercise> enriched = new ArrayList<>();
        for (MaterializedExercise me : session.getExercises()) {
            if (me.getLoad() == null && lastWeightByExercise.containsKey(me.getExercise().getName())) {
                enriched.add(me.toBuilder().load(lastWeightByExercise.get(me.getExercise().getName())).build());
            } else {
                enriched.add(me);
            }
        }
        return session.toBuilder().exercises(enriched).build();
    }

    public void recordNewMax(MajorLift lift, double newMaxWeight) throws JsonProcessingException {
        long currentTimeMillis = System.currentTimeMillis();

        TrainingMaxEntity trainingMaxEntity = new TrainingMaxEntity();
        trainingMaxEntity.lift = lift.getJsonName();
        trainingMaxEntity.valueLbs = newMaxWeight;
        trainingMaxEntity.effectiveAt = currentTimeMillis;

        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.eventId = UUID.randomUUID().toString();
        outboxEntity.eventType = EventType.TRAINING_MAX_UPDATED.getJsonName();
        outboxEntity.payload = objectMapper.writeValueAsString(trainingMaxEntity);
        outboxEntity.createdAt = currentTimeMillis;

        trainingMaxDao.insertMaxAndQueue(trainingMaxEntity, outboxEntity);
    }

    public Program getProgram(String programId) throws IOException {
        if (!programCache.containsKey(programId)) {
            ProgramTemplateEntity entity = programTemplateDao.selectProgramTemplate(programId);
            if (entity == null) {
                throw new IllegalStateException("Program not seeded: " + programId);
            }
            programCache.put(programId, ProgramLoader.loadProgram(
                    new ByteArrayInputStream(entity.jsonBlob.getBytes(StandardCharsets.UTF_8))));
        }
        return programCache.get(programId);
    }

    public String getActiveProgramId() {
        String id = appSettingsDao.getActiveProgramId();
        return id != null ? id : "powerbuilding_4x";
    }

    public void setActiveProgram(String programId) {
        executor.execute(() -> appSettingsDao.setActiveProgramId(programId));
    }

    public List<ProgramSummary> listAvailablePrograms() throws IOException {
        List<ProgramSummary> summaries = new ArrayList<>();
        for (String programId : programTemplateDao.selectAllProgramIds()) {
            Program program = getProgram(programId);
            summaries.add(new ProgramSummary(programId, program.getName()));
        }
        return summaries;
    }

    public void insertTrainingMax(TrainingMaxEntity trainingMaxEntity) {
        trainingMaxDao.insertMax(trainingMaxEntity);
    }

    public Long getLastPullSyncedAt() {
        return appSettingsDao.getLastPullSyncedAt();
    }

    public void setLastPullSyncedAt(Long since){
        appSettingsDao.setLastPullSyncedAt(since);
    }

    private String readAssetAsString(String filename) {
        try (InputStream in = assetManager.open(filename)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read bundled asset: " + filename, e);
        }
    }

    private void seedAppSettings() {
        AppSettingsEntity appSettingsEntity = new AppSettingsEntity();
        appSettingsEntity.id = 0;
        appSettingsEntity.activeProgramId = "powerbuilding_4x";
        appSettingsDao.ensureRowExists(appSettingsEntity);
    }

    private void seedOneProgram(String programId, String assetPath) throws IOException {
        String json = readAssetAsString(assetPath);
        ProgramTemplateEntity templateEntity = new ProgramTemplateEntity();
        templateEntity.programId = programId;
        templateEntity.jsonBlob = json;
        programTemplateDao.insertProgram(templateEntity);
    }

    private void ensureMaxesSeeded() throws IOException {
        List<TrainingMaxEntity> existing = trainingMaxDao.selectCurrentMaxes();
        Set<String> existingLifts = new HashSet<>();
        for (TrainingMaxEntity e : existing) {
            existingLifts.add(e.lift);
        }

        Program seedSource = getProgram("powerbuilding_4x");
        Map<String, Double> seedMaxes = seedSource.getOneRepMaxes();
        long now = System.currentTimeMillis();

        for (MajorLift lift : MajorLift.values()) {
            if (existingLifts.contains(lift.getJsonName())) {
                continue; // already has a max, don't touch it
            }
            Double maxVal = (seedMaxes != null) ? seedMaxes.get(lift.getJsonName()) : null;
            if (maxVal == null) {
                Log.w("ProgramRepository", "No seed max for " + lift.getDisplayName()
                        + " — using placeholder (45 lbs). Update it in Edit Maxes.");
                maxVal = 45.0;
            }
            TrainingMaxEntity maxEntity = new TrainingMaxEntity();
            maxEntity.lift = lift.getJsonName();
            maxEntity.valueLbs = maxVal;
            maxEntity.effectiveAt = now;
            trainingMaxDao.insertMax(maxEntity);
        }
    }
}
