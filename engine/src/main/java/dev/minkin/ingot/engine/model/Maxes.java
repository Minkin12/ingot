package dev.minkin.ingot.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
public class Maxes {
    private final EnumMap<MajorLift, Double> maxes;

    private Maxes(EnumMap<MajorLift, Double> maxes) {
        this.maxes = maxes;
    }

    /** Builds from a JSON-shaped map, e.g. {"squat": 260, "bench": 180, ...}.
     *  Throws on unknown MajorLift names or missing MajorLifts. */
    public static Maxes fromJsonMap(Map<String, Double> raw) {
        EnumMap<MajorLift, Double> result = new EnumMap<>(MajorLift.class);
        for (Map.Entry<String, Double> entry : raw.entrySet()) {
            result.put(MajorLift.fromJson(entry.getKey()), entry.getValue());
        }
        for (MajorLift MajorLift : MajorLift.values()) {
            if (!result.containsKey(MajorLift)) {
                throw new IllegalArgumentException("Missing training max for " + MajorLift);
            }
        }
        return new Maxes(result);
    }

    public double getMaxWeight(MajorLift MajorLift) {
        return maxes.get(MajorLift);
    }}
