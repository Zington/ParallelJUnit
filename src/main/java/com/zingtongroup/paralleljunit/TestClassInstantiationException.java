package com.zingtongroup.paralleljunit;

/**
 * Exception thrown when the default parameter-lass constructor of the test class cannot be instantiated.
 */
public class TestClassInstantiationException extends Exception {

    public TestClassInstantiationException(Exception e){
        super(e);
    }
}
