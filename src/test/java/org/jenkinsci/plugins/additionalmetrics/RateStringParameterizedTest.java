package org.jenkinsci.plugins.additionalmetrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RateStringParameterizedTest {

    static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {0, "0.00%"},
            {0.333333, "33.33%"},
            {0.5, "50.00%"},
            {0.666667, "66.67%"},
            {1, "100.00%"},
        });
    }

    @ParameterizedTest(name = "{index}: rate[{0}]={1}")
    @MethodSource("data")
    void test(double input, String expected) {
        Rate rate = new Rate(input);
        assertEquals(expected, rate.getAsString());
    }
}
