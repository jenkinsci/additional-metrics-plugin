package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Run;

/**
 * A record that pairs a Jenkins build run with its associated duration.
 * This is used to store and pass around run information along with calculated duration metrics.
 *
 * @param run the Jenkins build run
 * @param duration the calculated duration for the run
 */
public record RunWithDuration(Run run, Duration duration) {}
