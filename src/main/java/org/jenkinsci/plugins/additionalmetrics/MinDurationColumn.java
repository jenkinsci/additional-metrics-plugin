package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utils.findRun;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MinDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public MinDurationColumn() {
        super();
    }

    @Metric
    public RunWithDuration getShortestRun(Job<? extends Job, ? extends Run> job) {
        return findRun(job.getBuilds(), COMPLETED, RUN_DURATION, MIN).orElse(null);
    }

    @Extension
    @Symbol("minDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MinDurationColumn_DisplayName());
        }
    }
}
