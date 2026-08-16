package dev.minkin.ingot.data.remote.types;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BatchInsertResults {
    List<String> completedEvents;
    List<String> failedEvents;
}