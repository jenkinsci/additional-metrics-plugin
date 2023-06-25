package org.jenkinsci.plugins.additionalmetrics;

import hudson.Util;

public class Duration {
    private final long milliseconds;

    public Duration(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long getAsLong() {
        return milliseconds;
    }

    public String getAsString() {
        return Util.getTimeSpanString(milliseconds);
    }
}
