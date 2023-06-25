package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.NOT_SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.timeRateOf;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class FailureTimeRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public FailureTimeRateColumn() {
        super();
    }

    @Metric
    public Rate getFailureTimeRate(Job<? extends Job, ? extends Run> job) {
        return timeRateOf(job.getBuilds(), COMPLETED, NOT_SUCCESS).orElse(null);
    }

    @Extension
    @Symbol("failureTimeRate")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.FailureTimeRateColumn_DisplayName());
        }
    }
}
