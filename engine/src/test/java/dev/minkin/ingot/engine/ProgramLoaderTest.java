package dev.minkin.ingot.engine;

import static org.junit.jupiter.api.Assertions.*;

import dev.minkin.ingot.engine.model.Program;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class ProgramLoaderTest {

    @Test
    void loadProgram() throws IOException {

        try (InputStream in = getClass().getResourceAsStream("/program.json")) {
            assertNotNull(in, "program.json missing from src/test/resources");
            Program program = ProgramLoader.loadProgram(in);
            assertEquals(10, program.getWeeks().size());
        }

    }
}