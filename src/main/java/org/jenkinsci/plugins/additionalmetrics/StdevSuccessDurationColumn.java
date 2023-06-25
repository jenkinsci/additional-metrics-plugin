package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.RUN_DURATION;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.stdDevDuration;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class StdevSuccessDurationColumn extends ListViewColumn {
    @DataBoundConstructor
    public StdevSuccessDurationColumn() {
        super();
    }

    @Metric
    public Duration getStdevSuccessDuration(Job<? extends Job, ? extends Run> job) {
        return stdDevDuration(job.getBuilds(), SUCCESS, RUN_DURATION).orElse(null);
    }

    @Extension
    @Symbol("stdevSuccessDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.StdevSuccessDurationColumn_DisplayName());
        }
    }
}
