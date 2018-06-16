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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;
import hudson.model.Result;
import hudson.model.Run;

import java.io.Serializable;

class Helpers {

    static final Function<Iterable<? extends Run<?, ?>>, Run<?, ?>> MIN_DURATION = new MinDuration();
    static final Function<Iterable<? extends Run<?, ?>>, Run<?, ?>> MAX_DURATION = new MaxDuration();
    static final Predicate<Run<?, ?>> SUCCESS = new ResultPredicate(Result.SUCCESS);
    static final Predicate<Run<?, ?>> NOT_SUCCESS = Predicates.not(SUCCESS);

    private static final Ordering<Run<?, ?>> DURATION_ORDERING = new DurationOrdering();

    static class ResultPredicate implements Predicate<Run<?, ?>> {
        private final Result expectedResult;

        ResultPredicate(Result expectedResult) {
            this.expectedResult = expectedResult;
        }

        @Override
        public boolean apply(Run<?, ?> run) {
            return run.getResult() == expectedResult;
        }
    }

    private static class DurationOrdering extends Ordering<Run<?, ?>> implements Serializable {
        @Override
        public int compare(Run<?, ?> left, Run<?, ?> right) {
            return Long.compare(left.getDuration(), right.getDuration());
        }
    }

    private static class MinDuration implements Function<Iterable<? extends Run<?, ?>>, Run<?, ?>> {
        @Override
        public Run<?, ?> apply(Iterable<? extends Run<?, ?>> input) {
            return DURATION_ORDERING.min(input);
        }
    }

    private static class MaxDuration implements Function<Iterable<? extends Run<?, ?>>, Run<?, ?>> {
        @Override
        public Run<?, ?> apply(Iterable<? extends Run<?, ?>> input) {
            return DURATION_ORDERING.max(input);
        }
    }

}
