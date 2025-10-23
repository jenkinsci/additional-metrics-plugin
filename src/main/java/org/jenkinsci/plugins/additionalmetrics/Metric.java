package org.jenkinsci.plugins.additionalmetrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark methods that calculate and return metric values.
 * This annotation is used by the additional metrics plugin to identify methods
 * that provide metric data for Jenkins list view columns.
 * <p>
 * Methods annotated with @Metric should:
 * - Be public
 * - Return a metric value (Duration, Rate, RunWithDuration, etc.)
 * - Accept a Job parameter to calculate metrics for
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Metric {}
