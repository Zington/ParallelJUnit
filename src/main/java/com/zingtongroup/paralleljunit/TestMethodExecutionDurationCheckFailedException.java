package com.zingtongroup.paralleljunit;

/**
 * Exception thrown when a test takes longer than the given timeout.
 */
public class TestMethodExecutionDurationCheckFailedException extends Exception {

    public TestMethodExecutionDurationCheckFailedException(String message){
        super(message);
    }

}
