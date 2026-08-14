create table if not exists ingot.session_history
(
    event_id    UUID primary key,
    week_number INTEGER,
    day_number  INTEGER,
    workout_label TEXT,
    session_note TEXT,
    completed_at BIGINT,
    recorded_at BIGINT
)
