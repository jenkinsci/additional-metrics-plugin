package org.jenkinsci.plugins.additionalmetrics;

import static org.jenkinsci.plugins.additionalmetrics.Helpers.COMPLETED;
import static org.jenkinsci.plugins.additionalmetrics.Helpers.NOT_SUCCESS;
import static org.jenkinsci.plugins.additionalmetrics.Utils.rateOf;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A Jenkins list view column that displays the failure rate of completed builds for a job.
 * The failure rate is calculated as the percentage of completed builds that did not succeed.
 */
public class FailureRateColumn extends ListViewColumn {

    /**
     * Creates a new failure rate column.
     * This constructor is used by Jenkins for data binding.
     */
    @DataBoundConstructor
    public FailureRateColumn() {
        super();
    }

    /**
     * Calculates and returns the failure rate of completed builds for the specified job.
     * The failure rate includes all non-successful builds (failed, unstable, aborted, etc.).
     * Only considers builds that have completed (not currently building).
     *
     * @param job the Jenkins job to calculate the failure rate for
     * @return the failure rate as a Rate object, or null if no completed builds exist
     */
    @Metric
    public Rate getFailureRate(Job<? extends Job, ? extends Run> job) {
        return rateOf(job.getBuilds(), COMPLETED, NOT_SUCCESS).orElse(null);
    }

    @Extension
    @Symbol("failureRate")
    public static class DescriptorImpl extends AdditionalMetricColumnDescriptor {

        public DescriptorImpl() {
            super(Messages.FailureRateColumn_DisplayName());
        }
    }
}
