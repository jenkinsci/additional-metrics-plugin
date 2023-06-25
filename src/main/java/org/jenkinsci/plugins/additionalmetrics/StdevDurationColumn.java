package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.RUN_DURATION;
import static org.jenkinsci.plugins.additionalmetrics.Utils.stdDevDuration;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class StdevDurationColumn extends ListViewColumn {
    @DataBoundConstructor
    public StdevDurationColumn() {
        super();
    }

    @Metric
    public Duration getStdevDuration(Job<? extends Job, ? extends Run> job) {
        return stdDevDuration(job.getBuilds(), COMPLETED, RUN_DURATION).orElse(null);
    }

    @Extension
    @Symbol("stdevDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.StdevDurationColumn_DisplayName());
        }
    }
}
