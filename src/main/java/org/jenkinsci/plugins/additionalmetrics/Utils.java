package org.jenkinsci.plugins.additionalmetrics;

import com.google.common.collect.Iterables;
import hudson.model.Run;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class Utils {

    private Utils() {
        // utility class
    }

    static Optional<Rate> rateOf(List<? extends Run> runs, Predicate<Run> preFilter, Predicate<Run> predicateRate) {
        List<? extends Run> filteredRuns = preFilter(runs, preFilter).collect(Collectors.toList());

        if (filteredRuns.isEmpty()) {
            return Optional.empty();
        }

        int totalRuns = 0;
        int predicateApplicableRuns = 0;

        for (Run run : filteredRuns) {
            totalRuns++;
            if (predicateRate.test(run)) {
                predicateApplicableRuns++;
            }
        }

        return Optional.of(new Rate((double) predicateApplicableRuns / totalRuns));
    }

    static Optional<Rate> timeRateOf(List<? extends Run> runs, Predicate<Run> preFilter, Predicate<Run> predicateRate) {
        List<? extends Run> filteredRuns = preFilter(runs, preFilter).collect(Collectors.toList());

        if (filteredRuns.isEmpty()) {
            return Optional.empty();
        }

        Run firstRun = Iterables.getLast(filteredRuns, null);
        long startTime = firstRun.getStartTimeInMillis();
        long endTime = System.currentTimeMillis();

        long previousTime = endTime;
        long accumulatedPredicateTime = 0L;

        for (Run run : filteredRuns) {
            long runStartTime = run.getStartTimeInMillis();

            if (predicateRate.test(run)) {
                accumulatedPredicateTime += previousTime - runStartTime;
            }

            previousTime = runStartTime;
        }

        return Optional.of(new Rate((double) accumulatedPredicateTime / (endTime - startTime)));
    }

    static Optional<RunWithDuration> findRun(
            List<? extends Run> runs,
            Predicate<Run> preFilter,
            ToLongFunction<Run> durationFunction,
            BinaryOperator<RunWithDuration> operator) {
        return preFilter(runs, preFilter)
                .filter(r -> durationFunction.applyAsLong(r) > 0)
                .map(r -> new RunWithDuration(r, new Duration(durationFunction.applyAsLong(r))))
                .reduce(operator);
    }

    static Optional<Duration> averageDuration(
            List<? extends Run> runs, Predicate<Run> preFilter, ToLongFunction<Run> durationFunction) {
        return durationFunction(runs, preFilter, durationFunction, LongStream::average);
    }

    static Optional<Duration> stdDevDuration(
            List<? extends Run> runs, Predicate<Run> preFilter, ToLongFunction<Run> durationFunction) {
        return durationFunction(
                runs,
                preFilter,
                durationFunction,
                longStream -> MathCommons.standardDeviation(longStream.boxed().collect(Collectors.toList())));
    }

    private static Optional<Duration> durationFunction(
            List<? extends Run> runs,
            Predicate<Run> preFilter,
            ToLongFunction<Run> durationFunction,
            Function<LongStream, OptionalDouble> durationCollector) {
        LongStream longStream = preFilter(runs, preFilter)
                .filter(r -> durationFunction.applyAsLong(r) > 0)
                .mapToLong(durationFunction);

        OptionalDouble val = durationCollector.apply(longStream);

        if (val.isPresent()) {
            return Optional.of(new Duration((long) val.getAsDouble()));
        } else {
            return Optional.empty();
        }
    }

    private static Stream<? extends Run> preFilter(List<? extends Run> runs, Predicate<Run> preFilter) {
        return runs.stream().filter(preFilter);
    }
}
