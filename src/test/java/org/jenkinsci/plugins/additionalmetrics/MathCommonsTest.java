package org.jenkinsci.plugins.additionalmetrics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class MathCommonsTest {

    @Test
    void stdev_of_empty_durations_should_return_0() {
        assertEquals(0, MathCommons.standardDeviation(List.of()));
    }

    @Test
    void stdev_of_multiple_durations() {
        assertEquals(81.64965809277261, MathCommons.standardDeviation(List.of(100L, 200L, 300L)));
    }
}
