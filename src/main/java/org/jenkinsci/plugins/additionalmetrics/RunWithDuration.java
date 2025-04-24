package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Run;

public record RunWithDuration(Run run, Duration duration) {}
