package org.jenkinsci.plugins.additionalmetrics;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public record Rate(double rate) {
    public double getAsDouble() {
        return rate;
    }

    public String getAsString() {
        NumberFormat formatter = new DecimalFormat("0.00");
        return (formatter.format(rate * 100) + "%");
    }
}
