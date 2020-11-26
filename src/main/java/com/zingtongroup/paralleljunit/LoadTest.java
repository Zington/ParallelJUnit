package com.zingtongroup.paralleljunit;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables applying a load upon a system under test by enabling the same unit test method to be ran in concurrent parallel threads.
 * This test mechanism runs the test method in the given number of concurrent threads in a thread pool.
 * As soon a thread finishes a new one is started in the same thread pool slot.
 * Options include forceful shutdown at test duration end, a ramp-up period for warm-up of the system,
 * and a halt-on-error option to save systems from overload damage upon problems.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadTest {
    int maxThreadCount() default 2;
    long rampUpTimeInMilliseconds() default 0;
    boolean preEmptiveTestClassInstantiationWithTestClassObjectReUsedBetweenIterations() default false;
    long totalDurationInMilliseconds() default 3000;
    boolean haltOnError() default false;
    long maxExecutionTimeIndividualIteration() default -1;
    boolean abruptTerminationAtTestEnd() default true;
    int timeout() default 30000;
    Class<? extends Throwable> expected() default Test.None.class;
}
