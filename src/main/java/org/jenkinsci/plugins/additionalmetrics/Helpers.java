/*
 * MIT License
 *
 * Copyright (c) 2019 Chadi El Masri
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;
import hudson.model.Result;
import hudson.model.Run;

import java.io.Serializable;

import static org.jenkinsci.plugins.additionalmetrics.CheckoutDuration.checkoutDurationOf;

class Helpers {

    static final Function<Run, Long> RUN_DURATION = new DurationFunction();
    static final Function<Run, Long> RUN_CHECKOUT_DURATION = new CheckoutDurationFunction();

    static final Predicate<Run> SUCCESS = new ResultPredicate(Result.SUCCESS);
    static final Predicate<Run> NOT_SUCCESS = Predicates.not(SUCCESS);
    static final Predicate<Run> COMPLETED = new CompletedPredicate();

    private static final Ordering<RunWithDuration> DURATION_ORDERING = new DurationOrdering();

    static final Function<Iterable<RunWithDuration>, RunWithDuration> MIN = new Min(DURATION_ORDERING);
    static final Function<Iterable<RunWithDuration>, RunWithDuration> MAX = new Max(DURATION_ORDERING);


    private Helpers() {
        // utility class
    }

    static class ResultPredicate implements Predicate<Run> {
        private final Result expectedResult;

        ResultPredicate(Result expectedResult) {
            this.expectedResult = expectedResult;
        }

        @Override
        public boolean apply(Run run) {
            return run.getResult() == expectedResult;
        }
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

    private static class CompletedPredicate implements Predicate<Run> {
        @Override
        public boolean apply(Run input) {
            return !input.isBuilding();
        }
    }

    private static class DurationFunction implements Function<Run, Long> {
        @Override
        public Long apply(Run input) {
            return input.getDuration();
        }
    }

    private static class CheckoutDurationFunction implements Function<Run, Long> {
        @Override
        public Long apply(Run input) {
            return checkoutDurationOf(input);
        }
    }

}
