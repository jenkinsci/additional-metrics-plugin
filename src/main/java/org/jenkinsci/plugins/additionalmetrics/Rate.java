package org.jenkinsci.plugins.additionalmetrics;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Represents a rate as a double value between 0.0 and 1.0.
 * Provides methods to access the rate as a double or as a formatted percentage string.
 */
public record Rate(double rate) {

    /**
     * Returns the rate as a double value.
     *
     * @return the rate value as a double between 0.0 and 1.0
     */
    public double getAsDouble() {
        return rate;
    }

    /**
     * Returns the rate as a formatted percentage string.
     * The percentage is formatted to two decimal places with a "%" suffix.
     *
     * @return the rate formatted as a percentage string (e.g., "75.25%")
     */
    public String getAsString() {
        NumberFormat formatter = new DecimalFormat("0.00");
        return (formatter.format(rate * 100) + "%");
    }
}
