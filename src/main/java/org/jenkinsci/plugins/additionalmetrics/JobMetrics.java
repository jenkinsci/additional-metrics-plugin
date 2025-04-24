package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Job;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public record JobMetrics(Job job) {

    @Exported
    public long getAvgCheckoutDuration() {
        AvgCheckoutDurationColumn avgCheckoutDurationColumn = new AvgCheckoutDurationColumn();
        Duration avgCheckoutDuration = avgCheckoutDurationColumn.getAverageCheckoutDuration(job);
        return durationOrDefaultToZero(avgCheckoutDuration);
    }

    @Exported
    public long getAvgDuration() {
        AvgDurationColumn avgDurationColumn = new AvgDurationColumn();
        Duration avgDuration = avgDurationColumn.getAverageDuration(job);
        return durationOrDefaultToZero(avgDuration);
    }

    @Exported
    public long getAvgSuccessDuration() {
        AvgSuccessDurationColumn avgSuccessDurationColumn = new AvgSuccessDurationColumn();
        Duration avgSuccessDuration = avgSuccessDurationColumn.getAverageSuccessDuration(job);
        return durationOrDefaultToZero(avgSuccessDuration);
    }

    @Exported
    public long getMaxCheckoutDuration() {
        MaxCheckoutDurationColumn maxCheckoutDurationColumn = new MaxCheckoutDurationColumn();
        RunWithDuration longestCheckoutRun = maxCheckoutDurationColumn.getLongestCheckoutRun(job);
        return durationOrDefaultToZero(longestCheckoutRun);
    }

    @Exported
    public long getMaxDuration() {
        MaxDurationColumn maxDurationColumn = new MaxDurationColumn();
        RunWithDuration longestRun = maxDurationColumn.getLongestRun(job);
        return durationOrDefaultToZero(longestRun);
    }

    @Exported
    public long getMaxSuccessDuration() {
        MaxSuccessDurationColumn maxSuccessDurationColumn = new MaxSuccessDurationColumn();
        RunWithDuration longestSuccessfulRun = maxSuccessDurationColumn.getLongestSuccessfulRun(job);
        return durationOrDefaultToZero(longestSuccessfulRun);
    }

    @Exported
    public long getMinCheckoutDuration() {
        MinCheckoutDurationColumn minCheckoutDurationColumn = new MinCheckoutDurationColumn();
        RunWithDuration shortestCheckoutRun = minCheckoutDurationColumn.getShortestCheckoutRun(job);
        return durationOrDefaultToZero(shortestCheckoutRun);
    }

    @Exported
    public long getMinDuration() {
        MinDurationColumn minDurationColumn = new MinDurationColumn();
        RunWithDuration shortestRun = minDurationColumn.getShortestRun(job);
        return durationOrDefaultToZero(shortestRun);
    }

    @Exported
    public long getMinSuccessDuration() {
        MinSuccessDurationColumn minSuccessDurationColumn = new MinSuccessDurationColumn();
        RunWithDuration shortestSuccessfulRun = minSuccessDurationColumn.getShortestSuccessfulRun(job);
        return durationOrDefaultToZero(shortestSuccessfulRun);
    }

    @Exported
    public double getSuccessRate() {
        SuccessRateColumn successRateColumn = new SuccessRateColumn();
        Rate successRate = successRateColumn.getSuccessRate(job);
        return rateOrDefaultToZero(successRate);
    }

    @Exported
    public double getFailureRate() {
        FailureRateColumn failureRateColumn = new FailureRateColumn();
        Rate failureRate = failureRateColumn.getFailureRate(job);
        return rateOrDefaultToZero(failureRate);
    }

    @Exported
    public double getSuccessTimeRate() {
        SuccessTimeRateColumn successTimeRateColumn = new SuccessTimeRateColumn();
        Rate successTimeRate = successTimeRateColumn.getSuccessTimeRate(job);
        return rateOrDefaultToZero(successTimeRate);
    }

    @Exported
    public double getFailureTimeRate() {
        FailureTimeRateColumn failureTimeRateColumn = new FailureTimeRateColumn();
        Rate failureTimeRate = failureTimeRateColumn.getFailureTimeRate(job);
        return rateOrDefaultToZero(failureTimeRate);
    }

    @Exported
    public long getStandardDeviationDuration() {
        StdevDurationColumn stdevDurationColumn = new StdevDurationColumn();
        Duration standardDeviationDuration = stdevDurationColumn.getStdevDuration(job);
        return durationOrDefaultToZero(standardDeviationDuration);
    }

    @Exported
    public long getStandardDeviationSuccessDuration() {
        StdevSuccessDurationColumn stdevSuccessDurationColumn = new StdevSuccessDurationColumn();
        Duration standardDeviationSuccessDuration = stdevSuccessDurationColumn.getStdevSuccessDuration(job);
        return durationOrDefaultToZero(standardDeviationSuccessDuration);
    }

    @Exported
    public double getUnstableRate() {
        UnstableRateColumn unstableRateColumn = new UnstableRateColumn();
        Rate unstableRate = unstableRateColumn.getUnstableRate(job);
        return rateOrDefaultToZero(unstableRate);
    }

    private static double rateOrDefaultToZero(Rate rate) {
        if (rate != null) {
            return rate.getAsDouble();
        }
        return 0.0;
    }

    private static long durationOrDefaultToZero(Duration duration) {
        if (duration != null) {
            return duration.getAsLong();
        }
        return 0;
    }

    private static long durationOrDefaultToZero(RunWithDuration runWithDuration) {
        if (runWithDuration != null) {
            return runWithDuration.duration().getAsLong();
        }
        return 0;
    }
}
