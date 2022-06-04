/*
 * MIT License
 *
 * Copyright (c) 2022 Chadi El Masri, Oussama Ben Ghorbel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    private MathCommons() {
    }

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
        if (!average.isPresent())
            return OptionalDouble.empty();
        return average;
    }

    private static OptionalDouble standardDeviationDouble(Supplier<DoubleStream> streamSupplier) {
        OptionalDouble mean = MathCommons.averageDouble(streamSupplier::get);
        if (!mean.isPresent()) {
            return OptionalDouble.empty();
        } else {
            // Given the stream consumed is a finite sequence, if a mean is present, std follows.
            double variance = streamSupplier.get().map(d -> d - mean.getAsDouble())
                    .map(d -> d * d)
                    .average()
                    .getAsDouble();
            return OptionalDouble.of(Math.sqrt(variance));
        }
    }
}
