CREATE SCHEMA IF NOT EXISTS ingot;

create table if not exists ingot.events
(
    event_id    UUID primary key,
    event_type  TEXT,
    payload     JSONB,
    occurred_at BIGINT,
    recorded_at BIGINT
)