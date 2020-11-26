package com.zingtongroup.paralleljunit;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Normal JUnit Test but it executes the same test method in parallel in separate
 * threads and with separate test class instantiated objects.
 * May use a timeout to assert execution time and/or an expected thrown Exception.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConcurrencyTest {
    int threadCount() default 2;
    int timeout() default 0;
    Class<? extends Throwable> expected() default Test.None.class;
}