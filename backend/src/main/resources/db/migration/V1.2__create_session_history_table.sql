create table if not exists ingot.session_history
(
    event_id    UUID primary key,
    week_number NUMERIC,
    day_number  NUMERIC,
    session_note TEXT,
    completed_at BIGINT,
    recorded_at BIGINT
)
