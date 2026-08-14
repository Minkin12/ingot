package dev.minkin.ingot.backend.ingest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class BatchInsertResults {
    List<UUID> completedEvents;
    List<UUID> failedEvents;
}
