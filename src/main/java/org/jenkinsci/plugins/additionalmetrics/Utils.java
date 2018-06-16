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
import com.google.common.collect.Iterables;
import hudson.model.Run;

import javax.annotation.CheckForNull;


class Utils {

    @CheckForNull
    static Rate rateOf(Iterable<? extends Run<?, ?>> runs, Predicate<Run<?, ?>> predicate) {
        int totalRuns = 0;
        int predicateApplicableRuns = 0;

        for (Run<?, ?> run : runs) {
            totalRuns++;
            if (predicate.apply(run)) {
                predicateApplicableRuns++;
            }
        }

        if (totalRuns == 0) {
            return null;
        } else {
            return new Rate((double) predicateApplicableRuns / totalRuns);
        }
    }

    @CheckForNull
    static Run<?, ?> findRun(Iterable<? extends Run<?, ?>> runs, Predicate<Run<?, ?>> predicate, Function<Iterable<? extends Run<?, ?>>, Run<?, ?>> orderingFunction) {
        Iterable<? extends Run<?, ?>> filteredRuns = Iterables.filter(runs, predicate);

        if (Iterables.isEmpty(filteredRuns)) {
            return null;
        }

        return orderingFunction.apply(filteredRuns);
    }
}
