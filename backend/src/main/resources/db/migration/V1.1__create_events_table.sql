CREATE SCHEMA IF NOT EXISTS ingot;

create table if not exists ingot.events
(
    event_id    UUID primary key,
    event_type  TEXT   NOT NULL,
    payload     JSONB  NOT NULL,
    occurred_at BIGINT NOT NULL,
    recorded_at BIGINT NOT NULL
)