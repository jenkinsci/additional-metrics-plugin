package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Run;

public class RunWithDuration {
    private final Run run;
    private final Duration duration;

    RunWithDuration(Run run, Duration duration) {
        this.run = run;
        this.duration = duration;
    }

    public Run getRun() {
        return run;
    }

    public Duration getDuration() {
        return duration;
    }
}
