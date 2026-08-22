CREATE TABLE ingot.training_max_history
(
    id              BIGSERIAL PRIMARY KEY,
    lift            TEXT   NOT NULL,
    value_lbs       TEXT   NOT NULL,
    achieved_at     BIGINT NOT NULL,
    source_event_id UUID   NOT NULL
);