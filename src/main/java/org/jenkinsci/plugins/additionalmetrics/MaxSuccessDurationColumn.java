package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utils.findRun;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MaxSuccessDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public MaxSuccessDurationColumn() {
        super();
    }

    @Metric
    public RunWithDuration getLongestSuccessfulRun(Job<? extends Job, ? extends Run> job) {
        return findRun(job.getBuilds(), SUCCESS, RUN_DURATION, MAX).orElse(null);
    }

    @Extension
    @Symbol("maxSuccessDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MaxSuccessDurationColumn_DisplayName());
        }
    }
}
