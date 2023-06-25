package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utils.findRun;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class MinSuccessDurationColumn extends ListViewColumn {

    @DataBoundConstructor
    public MinSuccessDurationColumn() {
        super();
    }

    @Metric
    public RunWithDuration getShortestSuccessfulRun(Job<? extends Job, ? extends Run> job) {
        return findRun(job.getBuilds(), SUCCESS, RUN_DURATION, MIN).orElse(null);
    }

    @Extension
    @Symbol("minSuccessDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MinSuccessDurationColumn_DisplayName());
        }
    }
}
