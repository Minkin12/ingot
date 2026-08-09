package dev.minkin.ingot.data.db.repo;

import android.content.res.AssetManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import dev.minkin.ingot.data.db.entity.PerformedSetEventEntity;
import dev.minkin.ingot.data.db.entity.ProgramTemplateEntity;
import dev.minkin.ingot.data.db.entity.TrainingMaxEntity;
import dev.minkin.ingot.data.repo.ProgramTemplateDao;
import dev.minkin.ingot.data.repo.TrainingMaxDao;
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
    private ExecutorService executor;
    private AssetManager assetManager;
    public ProgramRepository(ProgramTemplateDao programTemplateDao, TrainingMaxDao trainingMaxDao, ExecutorService executor, AssetManager assetManager){
        this.programTemplateDao = programTemplateDao;
        this.trainingMaxDao = trainingMaxDao;
        this.executor = executor;
        this.assetManager = assetManager;
    }


    public void ensureSeeded() throws IOException {
        if (programTemplateDao.selectProgramTemplate() != null) {
            return; // already seeded — idempotent no-op
        }

        String json = readAssetAsString("program.json");
        Program program = ProgramLoader.loadProgram(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        ProgramTemplateEntity templateEntity = new ProgramTemplateEntity();
        templateEntity.id = 0;
        templateEntity.jsonBlob = json;
        executor.execute(() -> programTemplateDao.insertProgram(templateEntity));

        long now = System.currentTimeMillis();
        for (MajorLift lift : MajorLift.values()) {
            TrainingMaxEntity maxEntity = new TrainingMaxEntity();
            maxEntity.lift = lift.getJsonName();
            Double maxVal = program.getOneRepMaxes().get(lift.getJsonName());
            maxEntity.valueLbs = maxVal != null ? maxVal : 0.0;
            maxEntity.effectiveAt = now;
            executor.execute(() -> trainingMaxDao.insertMax(maxEntity));
        }
    }


    public Maxes getCurrentMaxes() {
        List<TrainingMaxEntity> rows = trainingMaxDao.selectCurrentMaxes();

        Map<String, Double> byJsonName = new HashMap<>();
        for (TrainingMaxEntity tme : rows) {
            byJsonName.put(tme.lift, tme.valueLbs);
        }
        return Maxes.fromJsonMap(byJsonName);
    }

    public MaterializedSession materializeSession(int weekNumber, int dayNumber) throws IOException {
        ProgramTemplateEntity entity = programTemplateDao.selectProgramTemplate();
        if (entity == null){
            throw new IllegalStateException("Program template not seeded");
        }
        Program program = ProgramLoader.loadProgram(
                new ByteArrayInputStream(entity.jsonBlob.getBytes(StandardCharsets.UTF_8)));


        return Materializer.materialize(program, getCurrentMaxes(), weekNumber,dayNumber);
    }

    public MaterializedSession enrich(MaterializedSession session,
                                                    Map<String, PerformedSetEventEntity> lastPerformedByExercise) throws IOException {

        List<MaterializedExercise> enriched = new ArrayList<>();
        for (MaterializedExercise me : session.getExercises()) {
            if (me.getLoad() == null) {
                PerformedSetEventEntity last = lastPerformedByExercise.get(me.getExercise().getName());
                if (last != null) {
                    me = me.toBuilder().load(last.weightLbs).build();
                }
            }
            enriched.add(me);
        }

        return session.toBuilder().exercises(enriched).build();
    }

    public void recordNewMax(MajorLift lift, double newMaxWeight){
        TrainingMaxEntity trainingMaxEntity = new TrainingMaxEntity();
        trainingMaxEntity.lift = lift.getJsonName();
        trainingMaxEntity.valueLbs = newMaxWeight;
        trainingMaxEntity.effectiveAt = System.currentTimeMillis();
        trainingMaxDao.insertMax(trainingMaxEntity);
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
}
