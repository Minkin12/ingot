package dev.minkin.ingot.engine;

public final class Rounding {
    private Rounding() {}

    public static double toNearestFive(double load) {
        return Math.round(load / 5.0) * 5.0;
    }
}
