package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MinCheckoutDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public MinCheckoutDurationColumn() {
        super();
    }

    @Metric
    public RunWithDuration getShortestCheckoutRun(Job<? extends Job, ? extends Run> job) {
        return Utils.findRun(job.getBuilds(), COMPLETED, RUN_CHECKOUT_DURATION, MIN)
                .orElse(null);
    }

    @Extension
    @Symbol("minCheckoutDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MinCheckoutDurationColumn_DisplayName());
        }
    }
}
