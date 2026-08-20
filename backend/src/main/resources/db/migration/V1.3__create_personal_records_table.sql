CREATE TABLE ingot.personal_records
(
    exercise_name   TEXT PRIMARY KEY,
    best_weight_lbs TEXT             NOT NULL,
    best_reps       INT              NOT NULL,
    estimated_1rm   DOUBLE PRECISION NOT NULL,
    achieved_at     BIGINT           NOT NULL,
    source_event_id UUID             NOT NULL
);