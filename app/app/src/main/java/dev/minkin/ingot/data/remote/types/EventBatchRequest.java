package dev.minkin.ingot.data.remote.types;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventBatchRequest {

    private List<Event> events;
}
