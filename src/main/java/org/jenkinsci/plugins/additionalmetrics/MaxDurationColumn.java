package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utils.findRun;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MaxDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public MaxDurationColumn() {
        super();
    }

    @Metric
    public RunWithDuration getLongestRun(Job<? extends Job, ? extends Run> job) {
        return findRun(job.getBuilds(), COMPLETED, RUN_DURATION, MAX).orElse(null);
    }

    @Extension
    @Symbol("maxDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MaxDurationColumn_DisplayName());
        }
    }
}
