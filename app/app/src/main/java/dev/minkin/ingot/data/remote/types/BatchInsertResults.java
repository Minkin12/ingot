package dev.minkin.ingot.data.remote.types;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BatchInsertResults {
    List<UUID> completedEvents;
    List<UUID> failedEvents;
}