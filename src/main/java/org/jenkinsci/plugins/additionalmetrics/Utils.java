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
import com.google.common.collect.Iterables;
import hudson.model.Run;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;


class Utils {

    private Utils() {
        // utility class
    }

    @CheckForNull
    static Rate rateOf(Iterable<? extends Run> runs, Predicate<Run> preFilter, Predicate<Run> predicateRate) {
        Iterable<? extends Run> filteredRuns = Iterables.filter(runs, preFilter);

        if (Iterables.isEmpty(filteredRuns)) {
            return null;
        }

        int totalRuns = 0;
        int predicateApplicableRuns = 0;

        for (Run run : filteredRuns) {
            totalRuns++;
            if (predicateRate.apply(run)) {
                predicateApplicableRuns++;
            }
        }

        return new Rate((double) predicateApplicableRuns / totalRuns);
    }

    @CheckForNull
    static Rate timeRateOf(Iterable<? extends Run> runs, Predicate<Run> preFilter, Predicate<Run> predicateRate) {
        Iterable<? extends Run> filteredRuns = Iterables.filter(runs, preFilter);

        if (Iterables.isEmpty(filteredRuns)) {
            return null;
        }

        Run firstRun = Iterables.getLast(filteredRuns, null);
        long startTime = firstRun.getStartTimeInMillis();
        long endTime = System.currentTimeMillis();

        long previousTime = endTime;
        long accumulatedPredicateTime = 0L;

        for (Run run : filteredRuns) {
            long runStartTime = run.getStartTimeInMillis();

            if (predicateRate.apply(run)) {
                accumulatedPredicateTime += previousTime - runStartTime;
            }

            previousTime = runStartTime;
        }

        return new Rate((double) accumulatedPredicateTime / (endTime - startTime));
    }

    @CheckForNull
    static RunWithDuration findRun(Iterable<? extends Run> runs, Predicate<Run> preFilter, final Function<Run, Long> durationFunction, Function<Iterable<RunWithDuration>, RunWithDuration> searchFunction) {
        Iterable<? extends Run> filteredRuns = Iterables.filter(runs, preFilter);

        if (Iterables.isEmpty(filteredRuns)) {
            return null;
        }

        List<RunWithDuration> runWithDurationList = new ArrayList<>();

        for (Run run : filteredRuns) {
            Long curDurationMs = durationFunction.apply(run);
            if (curDurationMs > 0) {
                runWithDurationList.add(new RunWithDuration(run, new Duration(curDurationMs)));
            }
        }

        if (runWithDurationList.isEmpty()) {
            return null;
        }

        return searchFunction.apply(runWithDurationList);
    }

    @CheckForNull
    static Duration averageDuration(Iterable<? extends Run> runs, Predicate<Run> preFilter, Function<Run, Long> durationFunction) {
        Iterable<? extends Run> filteredRuns = Iterables.filter(runs, preFilter);

        if (Iterables.isEmpty(filteredRuns)) {
            return null;
        }

        int totalRuns = 0;
        long totalDurations = 0;

        for (Run run : filteredRuns) {
            Long curDurationMs = durationFunction.apply(run);
            if (curDurationMs > 0) {
                totalRuns++;
                totalDurations += curDurationMs;
            }
        }

        if (totalRuns == 0) {
            return null;
        }

        return new Duration(totalDurations / totalRuns);
    }

}
