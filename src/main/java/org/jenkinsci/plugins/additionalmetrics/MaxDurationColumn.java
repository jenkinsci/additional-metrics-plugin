package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.*;
import static org.jenkinsci.plugins.additionalmetrics.Utils.findRun;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A Jenkins list view column that displays the longest running completed build for a job.
 * Shows both the build information and its duration.
 */
public class MaxDurationColumn extends ListViewColumn {

    /**
     * Creates a new maximum duration column.
     * This constructor is used by Jenkins for data binding.
     */
    @DataBoundConstructor
    public MaxDurationColumn() {
        super();
    }

    /**
     * Finds and returns the completed build with the longest duration for the specified job.
     * Only considers builds that have completed (not currently building) and have a positive duration.
     *
     * @param job the Jenkins job to find the longest running build for
     * @return a RunWithDuration containing the longest running build and its duration,
     *         or null if no completed builds with positive duration exist
     */
    @Metric
    public RunWithDuration getLongestRun(Job<? extends Job, ? extends Run> job) {
        return findRun(job.getBuilds(), COMPLETED, RUN_DURATION, MAX).orElse(null);
    }

    @Extension
    @Symbol("maxDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.MaxDurationColumn_DisplayName());
        }
    }
}
