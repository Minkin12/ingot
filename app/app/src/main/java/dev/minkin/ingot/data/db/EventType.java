package dev.minkin.ingot.data.db;

public enum EventType {
    WORKOUT_COMPLETED("workout_completed"),
    PERFORMED_SET("performed_set"),
    TRAINING_MAX_UPDATED("training_max_updated");

    private final String jsonName;

    EventType(String jsonName) {
        this.jsonName = jsonName;
    }

    public String getJsonName() {
        return jsonName;
    }

    public static EventType fromJsonName(String value) {
        for (EventType type : values()) {
            if (type.jsonName.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + value);
    }
}
