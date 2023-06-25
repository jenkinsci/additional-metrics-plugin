package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.rateOf;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class SuccessRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public SuccessRateColumn() {
        super();
    }

    @Metric
    public Rate getSuccessRate(Job<? extends Job, ? extends Run> job) {
        return rateOf(job.getBuilds(), COMPLETED, SUCCESS).orElse(null);
    }

    @Extension
    @Symbol("successRate")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.SuccessRateColumn_DisplayName());
        }
    }
}
