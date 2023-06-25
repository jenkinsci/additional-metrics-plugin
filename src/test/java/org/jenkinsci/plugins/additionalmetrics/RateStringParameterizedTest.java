package org.jenkinsci.plugins.additionalmetrics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RateStringParameterizedTest {

    private final double input;
    private final String expected;

    public RateStringParameterizedTest(double input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameters(name = "{index}: rate[{0}]={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {0, "0.00%"},
            {0.333333, "33.33%"},
            {0.5, "50.00%"},
            {0.666667, "66.67%"},
            {1, "100.00%"},
        });
    }

    @Test
    public void test() {
        Rate rate = new Rate(input);
        assertEquals(expected, rate.getAsString());
    }
}
