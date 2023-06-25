package org.jenkinsci.plugins.additionalmetrics;

import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

/**
 * Common math operations on lists
 * consumes {@link List} of {@link Number}
 * returns {@link OptionalDouble}.
 */
public final class MathCommons {

    private MathCommons() {}

    /**
     * @param list elements
     * @param <T>  the type of elements in this list
     * @return {@link OptionalDouble} average of the list
     */
    public static <T extends Number> OptionalDouble average(List<T> list) {
        return averageDouble(() -> list.stream().mapToDouble(t -> t.doubleValue()));
    }

    /**
     * @param list dataset
     * @param <T>  the type of elements in this dataset
     * @return {@link OptionalDouble} standard deviation of the dataset
     */
    public static <T extends Number> OptionalDouble standardDeviation(List<T> list) {
        return standardDeviationDouble(() -> list.stream().mapToDouble(t -> t.doubleValue()));
    }

    private static OptionalDouble averageDouble(Supplier<DoubleStream> streamSupplier) {
        OptionalDouble average = streamSupplier.get().average();
        if (!average.isPresent()) return OptionalDouble.empty();
        return average;
    }

    private static OptionalDouble standardDeviationDouble(Supplier<DoubleStream> streamSupplier) {
        OptionalDouble mean = MathCommons.averageDouble(streamSupplier::get);
        if (!mean.isPresent()) {
            return OptionalDouble.empty();
        } else {
            // Given the stream consumed is a finite sequence, if a mean is present, std follows.
            double variance = streamSupplier
                    .get()
                    .map(d -> d - mean.getAsDouble())
                    .map(d -> d * d)
                    .average()
                    .getAsDouble();
            return OptionalDouble.of(Math.sqrt(variance));
        }
    }
}
