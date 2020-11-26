package com.zingtongroup.paralleljunit;

/**
 * Exception thrown when a test method execution throws an Exception.
 */
public class TestMethodExecutionException extends Exception {

    public TestMethodExecutionException(Exception e){
        super(e);
    }
}
