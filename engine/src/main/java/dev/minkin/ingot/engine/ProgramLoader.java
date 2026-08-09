package dev.minkin.ingot.engine;

import com.fasterxml.jackson.databind.DeserializationFeature;
import dev.minkin.ingot.engine.model.Program;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ProgramLoader {
    public static Program loadProgram(InputStream in) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        return objectMapper.readValue(in, Program.class);

    }
}
