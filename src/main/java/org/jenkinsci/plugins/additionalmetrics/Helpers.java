/*
 * MIT License
 *
 * Copyright (c) 2020 Chadi El Masri
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

import com.google.common.collect.Ordering;
import hudson.model.Result;
import hudson.model.Run;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

class Helpers {

    static final ToLongFunction<Run> RUN_DURATION = Run::getDuration;
    static final ToLongFunction<Run> RUN_CHECKOUT_DURATION = CheckoutDuration::checkoutDurationOf;

    static final Predicate<Run> SUCCESS = run -> run.getResult() == Result.SUCCESS;
    static final Predicate<Run> NOT_SUCCESS = SUCCESS.negate();
    static final Predicate<Run> COMPLETED = run -> !run.isBuilding();

    private static final Ordering<RunWithDuration> DURATION_ORDERING = new DurationOrdering();

    static final Function<Iterable<RunWithDuration>, RunWithDuration> MIN = new Min(DURATION_ORDERING);
    static final Function<Iterable<RunWithDuration>, RunWithDuration> MAX = new Max(DURATION_ORDERING);

    private Helpers() {
        // utility class
    }

    private static class DurationOrdering extends Ordering<RunWithDuration> implements Serializable {
        @Override
        public int compare(RunWithDuration left, RunWithDuration right) {
            return Long.compare(left.getDuration().getAsLong(), right.getDuration().getAsLong());
        }
    }

    private static class Min implements Function<Iterable<RunWithDuration>, RunWithDuration> {
        private final Ordering<RunWithDuration> ordering;

        private Min(Ordering<RunWithDuration> ordering) {
            this.ordering = ordering;
        }

        @Override
        public RunWithDuration apply(Iterable<RunWithDuration> input) {
            return ordering.min(input);
        }
    }

    private static class Max implements Function<Iterable<RunWithDuration>, RunWithDuration> {
        private final Ordering<RunWithDuration> ordering;

        private Max(Ordering<RunWithDuration> ordering) {
            this.ordering = ordering;
        }

        @Override
        public RunWithDuration apply(Iterable<RunWithDuration> input) {
            return ordering.max(input);
        }
    }

}
