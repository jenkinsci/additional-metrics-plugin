package org.jenkinsci.plugins.additionalmetrics;

import java.util.List;

final class MathCommons {

    private MathCommons() {}

    static <N extends Number> double standardDeviation(List<N> numbers) {
        if (numbers.isEmpty()) {
            return 0;
        }

        double average =
                numbers.stream().mapToDouble(Number::longValue).average().getAsDouble();

        double variance = numbers.stream()
                .mapToDouble(Number::doubleValue)
                .map(d -> d - average)
                .map(d -> d * d)
                .average()
                .getAsDouble();

        return Math.sqrt(variance);
    }
}
