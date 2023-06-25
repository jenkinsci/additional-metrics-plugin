package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.timeRateOf;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class SuccessTimeRateColumn extends ListViewColumn {

    @DataBoundConstructor
    public SuccessTimeRateColumn() {
        super();
    }

    @Metric
    public Rate getSuccessTimeRate(Job<? extends Job, ? extends Run> job) {
        return timeRateOf(job.getBuilds(), COMPLETED, SUCCESS).orElse(null);
    }

    @Extension
    @Symbol("successTimeRate")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.SuccessTimeRateColumn_DisplayName());
        }
    }
}
