package org.jenkinsci.plugins.additionalmetrics;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Rate {
    private final double rate;

    Rate(double rate) {
        this.rate = rate;
    }

    public double getAsDouble() {
        return rate;
    }

    public String getAsString() {
        NumberFormat formatter = new DecimalFormat("0.00");
        return (formatter.format(rate * 100) + "%");
    }

    @Override
    public String toString() {
        return "Rate[" + rate + "]";
    }
}
