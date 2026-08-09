package dev.minkin.ingot.engine.model;

public enum MajorLift {
    SQUAT("squat"),
    BENCH("bench"),
    DEADLIFT("deadlift"),
    HIP_THRUST("hip_thrust");

    private final String jsonName;

    MajorLift(String jsonName) {
        this.jsonName = jsonName;
    }

    public String getJsonName() {
        return jsonName;
    }

    /** Maps a source_lift string from program.json to a Lift.
     *  Returns null for accessories (no source_lift in the JSON). */
    public static MajorLift fromJson(String value) {
        if (value == null) {
            return null;
        }
        for (MajorLift lift : values()) {
            if (lift.jsonName.equalsIgnoreCase(value)) {
                return lift;
            }
        }
        throw new IllegalArgumentException("Unknown source_lift: " + value);
    }
}
