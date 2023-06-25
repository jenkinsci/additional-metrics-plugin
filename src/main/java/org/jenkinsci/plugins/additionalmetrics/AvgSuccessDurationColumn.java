package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.RUN_DURATION;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.averageDuration;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class AvgSuccessDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public AvgSuccessDurationColumn() {
        super();
    }

    @Metric
    public Duration getAverageSuccessDuration(Job<? extends Job, ? extends Run> job) {
        return averageDuration(job.getBuilds(), SUCCESS, RUN_DURATION).orElse(null);
    }

    @Extension
    @Symbol("avgSuccessDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.AvgSuccessDurationColumn_DisplayName());
        }
    }
}
