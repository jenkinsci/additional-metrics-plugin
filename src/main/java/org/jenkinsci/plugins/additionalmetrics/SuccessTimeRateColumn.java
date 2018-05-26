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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ListIterator;

public class SuccessTimeRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public SuccessTimeRateColumn() {
        super();
    }

    public Rate getSuccessTimeRate(Job<? extends Job, ? extends Run> job) {
        RunList<? extends Run> runs = job.getBuilds().completedOnly();
        if (runs.isEmpty()) {
            return null;
        }

        List<? extends Run> runsOrdered = Lists.reverse(runs);

        Run firstRun = Iterables.getFirst(runsOrdered, null);
        long startTime = firstRun.getStartTimeInMillis();

        Run lastRun = Iterables.getLast(runsOrdered);
        long endTime = lastRun.getStartTimeInMillis() + lastRun.getDuration();

        long cumulatedFailedTime = 0;
        long failStartTime = -1;

        for (ListIterator<? extends Run> it = runsOrdered.listIterator(); it.hasNext(); ) {
            Run<?, ?> curRun = it.next();

            if (curRun.getResult() != Result.SUCCESS && failStartTime == -1) {
                failStartTime = curRun.getStartTimeInMillis();
            }

            if ((curRun.getResult() == Result.SUCCESS || !it.hasNext()) && failStartTime != -1) {
                cumulatedFailedTime += curRun.getStartTimeInMillis() + curRun.getDuration() - failStartTime;
                failStartTime = -1;
            }
        }

        return new Rate(1 - ((double) cumulatedFailedTime / (endTime - startTime)));
    }

    @Extension
    @Symbol("successTimeRate")
    public static class DescriptorImpl extends ListViewColumnDescriptor {

        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.SuccessTimeRateColumn_DisplayName();
        }

    }

}
