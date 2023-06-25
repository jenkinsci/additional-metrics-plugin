package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.UNSTABLE;
import static org.jenkinsci.plugins.additionalmetrics.Utils.rateOf;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class UnstableRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public UnstableRateColumn() {
        super();
    }

    @Metric
    public Rate getUnstableRate(Job<? extends Job, ? extends Run> job) {
        return rateOf(job.getBuilds(), COMPLETED, UNSTABLE).orElse(null);
    }

    @Extension
    @Symbol("unstableRate")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.UnstableRateColumn_DisplayName());
        }
    }
}
