package dev.minkin.ingot.engine.model;

public enum MajorLift {
    SQUAT("squat", "Squat"),
    BENCH("bench", "Bench"),
    DEADLIFT("deadlift", "Deadlift"),
    HIP_THRUST("hip_thrust", "Hip Thrust");

    private final String jsonName;
    private final String displayName;

    MajorLift(String jsonName, String displayName) {
        this.jsonName = jsonName;
        this.displayName = displayName;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MajorLift fromJson(String value) {
        for (MajorLift lift : values()) {
            if (lift.jsonName.equals(value)) {
                return lift;
            }
        }
        throw new IllegalArgumentException("Unknown source_lift: " + value);
    }
}