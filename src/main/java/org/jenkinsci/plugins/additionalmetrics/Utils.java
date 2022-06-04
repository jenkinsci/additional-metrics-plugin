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

import com.google.common.collect.Iterables;
import hudson.model.Run;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Utils {

    private Utils() {
        // utility class
    }

    static Optional<Rate> rateOf(List<? extends Run> runs, Predicate<Run> preFilter, Predicate<Run> predicateRate) {
        List<? extends Run> filteredRuns = preFilter(runs, preFilter)
                .collect(Collectors.toList());

        if (filteredRuns.isEmpty()) {
            return Optional.empty();
        }

        int totalRuns = 0;
        int predicateApplicableRuns = 0;

        for (Run run : filteredRuns) {
            totalRuns++;
            if (predicateRate.test(run)) {
                predicateApplicableRuns++;
            }
        }

        return Optional.of(new Rate((double) predicateApplicableRuns / totalRuns));
    }

    static Optional<Rate> timeRateOf(List<? extends Run> runs, Predicate<Run> preFilter, Predicate<Run> predicateRate) {
        List<? extends Run> filteredRuns = preFilter(runs, preFilter)
                .collect(Collectors.toList());

        if (filteredRuns.isEmpty()) {
            return Optional.empty();
        }

        Run firstRun = Iterables.getLast(filteredRuns, null);
        long startTime = firstRun.getStartTimeInMillis();
        long endTime = System.currentTimeMillis();

        long previousTime = endTime;
        long accumulatedPredicateTime = 0L;

        for (Run run : filteredRuns) {
            long runStartTime = run.getStartTimeInMillis();

            if (predicateRate.test(run)) {
                accumulatedPredicateTime += previousTime - runStartTime;
            }

            previousTime = runStartTime;
        }

        return Optional.of(new Rate((double) accumulatedPredicateTime / (endTime - startTime)));
    }

    static Optional<RunWithDuration> findRun(List<? extends Run> runs, Predicate<Run> preFilter, ToLongFunction<Run> durationFunction, BinaryOperator<RunWithDuration> operator) {
        return preFilter(runs, preFilter)
                .filter(r -> durationFunction.applyAsLong(r) > 0)
                .map(r -> new RunWithDuration(r, new Duration(durationFunction.applyAsLong(r))))
                .reduce(operator);
    }

    static Optional<Duration> averageDuration(List<? extends Run> runs, Predicate<Run> preFilter, ToLongFunction<Run> durationFunction) {
        OptionalDouble average = MathCommons.average(preFilter(runs, preFilter)
                .filter(r -> durationFunction.applyAsLong(r) > 0)
                .mapToLong(durationFunction)
                .boxed()
                .collect(Collectors.toList()));
        if (average.isPresent()) {
            return Optional.of(new Duration((long) average.getAsDouble()));
        } else {
            return Optional.empty();
        }
    }

    static Optional<Duration> standardDeviationDuration(List<? extends Run> runs, Predicate<Run> preFilter, ToLongFunction<Run> durationFunction) {
        List durations = preFilter(runs, preFilter)
                .filter(r -> durationFunction.applyAsLong(r) > 0)
                .mapToLong(durationFunction)
                .boxed()
                .collect(Collectors.toList());

        OptionalDouble std = MathCommons.standardDeviation(durations);
        if (!std.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new Duration((long) std.getAsDouble()));
    }


    private static Stream<? extends Run> preFilter(List<? extends Run> runs, Predicate<Run> preFilter) {
        return runs.stream()
                .filter(preFilter);
    }

}
