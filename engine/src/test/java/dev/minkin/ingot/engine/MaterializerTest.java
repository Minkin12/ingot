package dev.minkin.ingot.engine;



import dev.minkin.ingot.engine.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tier 1: hand-picked known values from the spreadsheet.
 * Tier 2: behavioral properties (idempotency, maxes sensitivity, ordering).
 * Tier 3: the oracle — every stored load in program.json is reproduced.
 */
class MaterializerTest {

    private static Program program;
    private static Maxes seedMaxes;

    @BeforeAll
    static void load() throws Exception {
        try (InputStream in = MaterializerTest.class.getResourceAsStream("/program.json")) {
            assertNotNull(in, "program.json missing from engine/src/test/resources");
            program = ProgramLoader.loadProgram(in);
        }

        seedMaxes = Maxes.fromJsonMap(Map.of(
                "squat", 260.0,
                "bench", 180.0,
                "deadlift", 275.0,
                "hip_thrust", 275.0));
    }

    // ---------- Tier 1: known values ----------

    @Test
    void computesTrackedLiftLoadFromPercentage() {
        // Week 1, day 1: Back Squat (Backoff) — 0.711 × 260 = 184.86 → 185
        MaterializedSession s = Materializer.materialize(program, seedMaxes, 1, 1);
        assertEquals("185.0", exerciseNamed(s, "Back Squat (Backoff)").getLoad());
    }

    @Test
    void derivedLiftUsesItsSourceLiftsMax() {
        // Week 1 RDL draws from the DEADLIFT max: 0.586 × 275 = 161.15 → 160
        MaterializedSession s = Materializer.materialize(program, seedMaxes, 1, 1);
        assertEquals("160.0", exerciseNamed(s, "Romanian Deadlift").getLoad());
    }

    @Test
    void accessoriesMaterializeWithNullLoad() {
        // No pct, no fixed load → null (blank in the UI), not zero, not an exception.
        MaterializedSession s = Materializer.materialize(program, seedMaxes, 1, 2);
        assertNull(exerciseNamed(s, "Seated DB Shoulder Press").getLoad());
    }

    @Test
    void deloadWeekUsesItsOwnPercentages() {
        // Week 5 (deload), day 1: squat top set — 0.65 × 260 = 169 → 170
        MaterializedSession s = Materializer.materialize(program, seedMaxes, 5, 1);
        assertEquals("170.0", exerciseNamed(s, "Back Squat (Top Set)").getLoad());
    }

    @Test
    void sessionCarriesItsCoordinatesAndLabel() {
        MaterializedSession s = Materializer.materialize(program, seedMaxes, 1, 2);
        assertEquals(1, s.getWeekNumber());
        assertEquals(2, s.getDayNumber());
        assertEquals("FULL BODY 2", s.getLabel());
    }

    @Test
    void invalidCoordinatesThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> Materializer.materialize(program, seedMaxes, 11, 1));
        assertThrows(IllegalArgumentException.class,
                () -> Materializer.materialize(program, seedMaxes, 1, 5));
        assertThrows(IllegalArgumentException.class,
                () -> Materializer.materialize(program, seedMaxes, 0, 1));
    }



    @Test
    void differentMaxesProduceDifferentLoads() {
        Maxes stronger = Maxes.fromJsonMap(Map.of(
                "squat", 300.0,   // +40
                "bench", 180.0,
                "deadlift", 275.0,
                "hip_thrust", 275.0));
        double before = Double.parseDouble(exerciseNamed(
                Materializer.materialize(program, seedMaxes, 2, 1), "Back Squat (Top Set)").getLoad());
        double after = Double.parseDouble(exerciseNamed(
                Materializer.materialize(program, stronger, 2, 1), "Back Squat (Top Set)").getLoad());
        assertTrue(after > before,
                "stronger maxes should raise the squat top set (" + before + " -> " + after + ")");
    }

    @Test
    void materializationIsIdempotent() {
        MaterializedSession first = Materializer.materialize(program, seedMaxes, 3, 2);
        MaterializedSession second = Materializer.materialize(program, seedMaxes, 3, 2);
        assertEquals(first.getExercises().size(), second.getExercises().size());
        for (int i = 0; i < first.getExercises().size(); i++) {
            assertEquals(first.getExercises().get(i).getLoad(),
                    second.getExercises().get(i).getLoad(),
                    "load drifted on repeat call at exercise index " + i
                            + " — is Materializer mutating the template?");
        }
    }

    @Test
    void preservesExerciseOrderFromTheTemplate() {
        Day day = program.getWeeks().get(0).getDays().get(0);   // week 1, day 1 template
        MaterializedSession s = Materializer.materialize(program, seedMaxes, 1, 1);
        assertEquals(day.getExercises().size(), s.getExercises().size());
        for (int i = 0; i < day.getExercises().size(); i++) {
            assertEquals(day.getExercises().get(i).getName(),
                    s.getExercises().get(i).getExercise().getName(),
                    "exercise order diverged from template at index " + i);
        }
    }

    // ---------- Tier 3: the oracle ----------

    @Test
    void everyStoredLoadInTheProgramIsReproduced() {
        int checked = 0;
        for (Week week : program.getWeeks()) {
            for (Day day : week.getDays()) {
                MaterializedSession s = Materializer.materialize(
                        program, seedMaxes, week.getNumber(), day.getDayNumber());
                for (MaterializedExercise me : s.getExercises()) {
                    Exercise ex = me.getExercise();
                    // Only percentage-driven rows are materialization outputs;
                    // ex.getLoad() here is the stored spreadsheet value (oracle data).
                    if (ex.getPctOneRepMax() != null && ex.getLoad() != null) {
                        assertEquals(Double.parseDouble(ex.getLoad()), Double.parseDouble(me.getLoad()),
                                "week " + week.getNumber()
                                        + " day " + day.getDayNumber()
                                        + " — " + ex.getName());
                        checked++;
                    }
                }
            }
        }

        assertTrue(checked > 75, "oracle only verified " + checked + " rows — expected 75+");
    }



    private static MaterializedExercise exerciseNamed(MaterializedSession session, String name) {
        List<MaterializedExercise> all = session.getExercises();
        for (MaterializedExercise me : all) {
            if (name.equals(me.getExercise().getName())) {
                return me;
            }
        }
        fail("no exercise named '" + name + "' in " + session.getLabel()
                + " — found: " + all.stream()
                        .map(m -> m.getExercise().getName()).toList());
        return null;
    }
}