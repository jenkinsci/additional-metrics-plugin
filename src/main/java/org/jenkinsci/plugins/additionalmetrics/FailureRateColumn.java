package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.NOT_SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.rateOf;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class FailureRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public FailureRateColumn() {
        super();
    }

    @Metric
    public Rate getFailureRate(Job<? extends Job, ? extends Run> job) {
        return rateOf(job.getBuilds(), COMPLETED, NOT_SUCCESS).orElse(null);
    }

    @Extension
    @Symbol("failureRate")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.FailureRateColumn_DisplayName());
        }
    }
}
