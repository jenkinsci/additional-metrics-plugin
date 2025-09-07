package org.jenkinsci.plugins.additionalmetrics;

import hudson.Util;

/**
 * Represents a time duration in milliseconds.
 * Provides methods to access the duration as a long value or as a human-readable string.
 */
public record Duration(long milliseconds) {

    /**
     * Returns the duration in milliseconds.
     *
     * @return the duration value in milliseconds
     */
    public long getAsLong() {
        return milliseconds;
    }

    /**
     * Returns the duration as a human-readable time span string.
     * Uses Jenkins' utility method to format the duration (e.g., "2 hr 30 min").
     *
     * @return the duration formatted as a human-readable string
     */
    public String getAsString() {
        return Util.getTimeSpanString(milliseconds);
    }
}
