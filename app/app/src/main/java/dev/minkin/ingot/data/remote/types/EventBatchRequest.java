package dev.minkin.ingot.data.remote.types;

import java.util.List;

import lombok.Getter;

@Getter
public class EventBatchRequest {

    private List<Event> events;
}
