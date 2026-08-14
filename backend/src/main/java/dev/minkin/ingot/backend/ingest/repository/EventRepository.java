package dev.minkin.ingot.backend.ingest.repository;

import dev.minkin.ingot.backend.ingest.model.Event;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {
    private final JdbcClient  jdbcClient;

    public EventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean insertEventIfNew(Event event){
        if (event == null){
            return false;
        }
        int rows = jdbcClient.sql("""
                INSERT INTO ingot.events (event_id, event_type, payload, occurred_at, recorded_at)
                VALUES (:id, :type, :payload::jsonb, :occurredAt, :recordedAt)
                ON CONFLICT (event_id) DO NOTHING
                """)
                .param("id", event.getEventId())
                .param("type", event.getEventType())
                .param("payload", event.getPayload())
                .param("occurredAt", event.getCompletedAt())
                .param("recordedAt", System.currentTimeMillis())
                .update();
        return rows > 0;
    }
}
