package org.jenkinsci.plugins.additionalmetrics;

import hudson.Util;

public record Duration(long milliseconds) {
    public long getAsLong() {
        return milliseconds;
    }

    public String getAsString() {
        return Util.getTimeSpanString(milliseconds);
    }
}
