/*
 * MIT License
 *
 * Copyright (c) 2018 Chadi El Masri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.additionalmetrics;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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
        return Arrays.asList(
                new Object[][]{
                        {0, "0.00%"},
                        {0.333333, "33.33%"},
                        {0.5, "50.00%"},
                        {0.666667, "66.67%"},
                        {1, "100.00%"}
                }
        );
    }

    @Test
    public void test() {
        Rate rate = new Rate(input);
        assertEquals(expected, rate.getAsString());
    }
}