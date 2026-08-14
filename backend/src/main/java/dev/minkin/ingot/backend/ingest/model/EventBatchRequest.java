package dev.minkin.ingot.backend.ingest.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class EventBatchRequest {
    @NotEmpty
    @Size(max = 500)
    @Valid
    List<Event> events;
}
