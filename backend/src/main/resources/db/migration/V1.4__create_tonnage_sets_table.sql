CREATE TABLE ingot.tonnage_sets
(
    id              BIGSERIAL PRIMARY KEY,
    week_number     INT  NOT NULL,
    day_number      INT  NOT NULL,
    exercise_name   TEXT NOT NULL,
    set_number      INT  NOT NULL,
    weight_lbs      TEXT NOT NULL,
    reps            INT  NOT NULL,
    source_event_id UUID NOT NULL,
    UNIQUE (week_number, day_number, exercise_name, set_number)
);