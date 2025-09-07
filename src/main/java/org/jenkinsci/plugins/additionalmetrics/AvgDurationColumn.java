package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.RUN_DURATION;
import static org.jenkinsci.plugins.additionalmetrics.Utils.averageDuration;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A Jenkins list view column that displays the average duration of completed builds for a job.
 */
public class AvgDurationColumn extends ListViewColumn {

    /**
     * Creates a new average duration column.
     * This constructor is used by Jenkins for data binding.
     */
    @DataBoundConstructor
    public AvgDurationColumn() {
        super();
    }

    /**
     * Calculates and returns the average duration of completed builds for the specified job.
     * Only considers builds that have completed (not currently building).
     *
     * @param job the Jenkins job to calculate the average duration for
     * @return the average duration of completed builds, or null if no completed builds exist
     */
    @Metric
    public Duration getAverageDuration(Job<? extends Job, ? extends Run> job) {
        return averageDuration(job.getBuilds(), COMPLETED, RUN_DURATION).orElse(null);
    }

    @Extension
    @Symbol("avgDuration")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.AvgDurationColumn_DisplayName());
        }
    }
}
