package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Job;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class JobMetrics {
    private final Job job;

    JobMetrics(Job job) {
        this.job = job;
    }

    @Exported
    public long getAvgCheckoutDuration() {
        AvgCheckoutDurationColumn avgCheckoutDurationColumn = new AvgCheckoutDurationColumn();
        Duration avgCheckoutDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(job);
        if (avgCheckoutDuration != null) {
            return avgCheckoutDuration.getAsLong();
        }
        return 0;
    }

    @Exported
    public long getAvgDuration() {
        AvgDurationColumn avgDurationColumn = new AvgDurationColumn();
        Duration avgDuration = avgDurationColumn.getAverageDuration(job);
        if (avgDuration != null) {
            return avgDuration.getAsLong();
        }
        return 0;
    }

    @Exported
    public long getAvgSuccessDuration() {
        AvgSuccessDurationColumn avgSuccessDurationColumn = new AvgSuccessDurationColumn();
        Duration avgSuccessDuration = avgSuccessDurationColumn.getAverageSuccessDuration(job);
        if (avgSuccessDuration != null) {
            return avgSuccessDuration.getAsLong();
        }
        return 0;
    }

    @Exported
    public long getMaxCheckoutDuration() {
        MaxCheckoutDurationColumn maxCheckoutDurationColumn = new MaxCheckoutDurationColumn();
        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(job);
        if (longestCheckoutRun != null) {
            return longestCheckoutRun.getDuration().getAsLong();
        }
        return 0;
    }

    @Exported
    public long getMaxDuration() {
        MaxDurationColumn maxDurationColumn = new MaxDurationColumn();
        RunWithDuration longestRun = maxDurationColumn.getLongestRun(job);
        if (longestRun != null) {
            return longestRun.getDuration().getAsLong();
        }
        return 0;
    }

    @Exported
    public long getMaxSuccessDuration() {
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();
        RunWithDuration longestSuccessfulRun = maxSuccessDurationColumn.getLongestSuccessfulRun(job);
        if (longestSuccessfulRun != null) {
            return longestSuccessfulRun.getDuration().getAsLong();
        }
        return 0;
    }

    @Exported
    public long getMinCheckoutDuration() {
        MinCheckoutDurationColumn minCheckoutDurationColumn = new MinCheckoutDurationColumn();
        RunWithDuration shortestCheckoutRun = minCheckoutDurationColumn.getShortestCheckoutRun(job);
        if (shortestCheckoutRun != null) {
            return shortestCheckoutRun.getDuration().getAsLong();
        }
        return 0;
    }

    @Exported
    public long getMinDuration() {
        MinDurationColumn minDurationColumn = new MinDurationColumn();
        RunWithDuration shortestRun = minDurationColumn.getShortestRun(job);
        if (shortestRun != null) {
            return shortestRun.getDuration().getAsLong();
        }
        return 0;
    }

    @Exported
    public long getMinSuccessDuration() {
        MinSuccessDurationColumn minSuccessDurationColumn = new MinSuccessDurationColumn();
        RunWithDuration shortestSuccessfulRun = minSuccessDurationColumn.getShortestSuccessfulRun(job);
        if (shortestSuccessfulRun != null) {
            return shortestSuccessfulRun.getDuration().getAsLong();
        }
        return 0;
    }

    @Exported
    public double getSuccessRate() {
        SuccessRateColumn successRateColumn = new SuccessRateColumn();
        Rate successRate = successRateColumn.getSuccessRate(job);
        if (successRate != null) {
            return successRate.getAsDouble();
        }
        return 0.0;
    }

    @Exported
    public double getFailureRate() {
        FailureRateColumn failureRateColumn = new FailureRateColumn();
        Rate failureRate = failureRateColumn.getFailureRate(job);
        if (failureRate != null) {
            return failureRate.getAsDouble();
        }
        return 0.0;
    }

    @Exported
    public double getSuccessTimeRate() {
        SuccessTimeRateColumn successTimeRateColumn = new SuccessTimeRateColumn();
        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(job);
        if (successTimeRate != null) {
            return successTimeRate.getAsDouble();
        }
        return 0.0;
    }

    @Exported
    public double getFailureTimeRate() {
        FailureTimeRateColumn failureTimeRateColumn = new FailureTimeRateColumn();
        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(job);
        if (failureTimeRate != null) {
            return failureTimeRate.getAsDouble();
        }
        return 0.0;
    }

    @Exported
    public long getStandardDeviationDuration() {
        StdevDurationColumn stdevDurationColumn = new StdevDurationColumn();
        Duration standardDeviationDuration = stdevDurationColumn.getStdevDuration(job);
        if (standardDeviationDuration != null) {
            return standardDeviationDuration.getAsLong();
        }

        return 0;
    }

    @Exported
    public long getStandardDeviationSuccessDuration() {
        StdevSuccessDurationColumn stdevSuccessDurationColumn = new StdevSuccessDurationColumn();
        Duration standardDeviationSuccessDuration = stdevSuccessDurationColumn.getStdevSuccessDuration(job);
        if (standardDeviationSuccessDuration != null) {
            return standardDeviationSuccessDuration.getAsLong();
        }

        return 0;
    }

    @Exported
    public double getUnstableRate() {
        UnstableRateColumn unstableRateColumn = new UnstableRateColumn();
        Rate unstableRate = unstableRateColumn.getUnstableRate(job);
        if (unstableRate != null) {
            return unstableRate.getAsDouble();
        }
        return 0.0;
    }
}
