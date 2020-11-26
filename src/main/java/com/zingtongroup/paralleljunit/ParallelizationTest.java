package com.zingtongroup.paralleljunit;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Executes the test first once to populate caches,
 * then one more time in one single thread to benchmark
 * test duration with execution in a single thread, and
 * lastly executes with multiple threads to check if the
 * execution time is significantly longer with parallel
 * threads.
 * Test class instantiation is not included in test
 * duration check.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParallelizationTest {
    int multipleThreadsCount() default 3;
    double maxExecutionDurationMultipleForMultipleThreadsExecution() default 1.5;
    Class<? extends Throwable> expected() default Test.None.class;
}
