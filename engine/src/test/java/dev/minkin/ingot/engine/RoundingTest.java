package dev.minkin.ingot.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundingTest {

    @Test
    void roundsToNearestFive() {
        assertEquals(215.0, Rounding.toNearestFive(260 * 0.836));
        assertEquals(220.0, Rounding.toNearestFive(218.0));
        assertEquals(0.0, Rounding.toNearestFive(2.4));
    }
}
