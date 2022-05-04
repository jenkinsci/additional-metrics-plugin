/*
 * MIT License
 *
 * Copyright (c) 2022 Chadi El Masri
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

import hudson.model.Result;
import hudson.model.Run;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

class Helpers {

    static final ToLongFunction<Run> RUN_DURATION = Run::getDuration;
    static final ToLongFunction<Run> RUN_CHECKOUT_DURATION = CheckoutDuration::checkoutDurationOf;

    static final Predicate<Run> SUCCESS = run -> run.getResult() == Result.SUCCESS;
    static final Predicate<Run> NOT_SUCCESS = SUCCESS.negate();
    static final Predicate<Run> COMPLETED = run -> !run.isBuilding();

    private static final Comparator<RunWithDuration> DURATION_ORDERING = Comparator.comparing(runWithDuration -> runWithDuration.getDuration().getAsLong());

    static final BinaryOperator<RunWithDuration> MIN = BinaryOperator.minBy(DURATION_ORDERING);
    static final BinaryOperator<RunWithDuration> MAX = BinaryOperator.maxBy(DURATION_ORDERING);

    private Helpers() {
        // utility class
    }


}
