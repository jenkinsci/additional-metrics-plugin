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

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.RUN_DURATION;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.stdDevDuration;
import org.kohsuke.stapler.DataBoundConstructor;

public class StdevSuccessDurationColumn extends ListViewColumn {
    @DataBoundConstructor
    public StdevSuccessDurationColumn() {
        super();
    }

    @Metric
    public Duration getStdevSuccessDuration(Job<? extends Job, ? extends Run> job) {
        return stdDevDuration(
                job.getBuilds(),
                SUCCESS,
                RUN_DURATION
        ).orElse(null);
    }

    @Extension
    @Symbol("stdevSuccessDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.StdevSuccessDurationColumn_DisplayName());
        }

    }

}
