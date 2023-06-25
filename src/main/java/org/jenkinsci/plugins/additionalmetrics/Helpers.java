package org.jenkinsci.plugins.additionalmetrics;

import hudson.model.Result;
import hudson.model.Run;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

class Helpers {

    static final ToLongFunction<Run> RUN_DURATION = Run::getDuration;
    static final ToLongFunction<Run> RUN_CHECKOUT_DURATION = CheckoutDuration::checkoutDurationOf;

    static final Predicate<Run> SUCCESS = run -> run.getResult() == Result.SUCCESS;
    static final Predicate<Run> UNSTABLE = run -> run.getResult() == Result.UNSTABLE;
    static final Predicate<Run> NOT_SUCCESS = SUCCESS.negate();
    static final Predicate<Run> COMPLETED = run -> !run.isBuilding();

    private static final Comparator<RunWithDuration> DURATION_ORDERING = Comparator.comparing(
            runWithDuration -> runWithDuration.getDuration().getAsLong());

    static final BinaryOperator<RunWithDuration> MIN = BinaryOperator.minBy(DURATION_ORDERING);
    static final BinaryOperator<RunWithDuration> MAX = BinaryOperator.maxBy(DURATION_ORDERING);

    private Helpers() {
        // utility class
    }
}
