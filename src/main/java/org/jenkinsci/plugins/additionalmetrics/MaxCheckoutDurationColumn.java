package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utils.findRun;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MaxCheckoutDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public MaxCheckoutDurationColumn() {
        super();
    }

    @Metric
    public RunWithDuration getLongestCheckoutRun(Job<? extends Job, ? extends Run> job) {
        return findRun(job.getBuilds(), COMPLETED, RUN_CHECKOUT_DURATION, MAX).orElse(null);
    }

    @Extension
    @Symbol("maxCheckoutDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MaxCheckoutDurationColumn_DisplayName());
        }
    }
}
