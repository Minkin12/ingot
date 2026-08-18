package dev.minkin.ingot.data.repo.types;

import lombok.Getter;

@Getter
public class ProgramSummary {
    private final String programId;
    private final String name;

    public ProgramSummary(String programId, String name) {
        this.programId = programId;
        this.name = name;
    }

}