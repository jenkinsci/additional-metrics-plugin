package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.RUN_CHECKOUT_DURATION;
import static org.jenkinsci.plugins.additionalmetrics.Utils.averageDuration;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class AvgCheckoutDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public AvgCheckoutDurationColumn() {
        super();
    }

    @Metric
    public Duration getAverageCheckoutDuration(Job<? extends Job, ? extends Run> job) {
        return averageDuration(job.getBuilds(), COMPLETED, RUN_CHECKOUT_DURATION)
                .orElse(null);
    }

    @Extension
    @Symbol("avgCheckoutDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.AvgCheckoutDurationColumn_DisplayName());
        }
    }
}
