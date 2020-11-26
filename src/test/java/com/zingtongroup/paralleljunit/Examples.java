package com.zingtongroup.paralleljunit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(ParallelJUnit.class)
public class Examples {

    @Test
    public void passingRegularTestShouldPass(){
        Assert.assertTrue(true);
    }

    @ConcurrencyTest(threadCount = 4, timeout = 200)
    public void parallelTestShouldPass() throws InterruptedException {
        Assert.assertTrue(true);
        Thread.sleep(100);
        Assert.assertTrue(true);
    }

    @ConcurrencyTest(threadCount = 10, expected = TestMethodExecutionException.class)
    public void exceptionThrowingTestMethodShouldThrowException() throws Exception {
        Assert.assertTrue(true);
        if(Math.random() * 100 < 50){
            throw new Exception("Oups");
        }
        Assert.assertTrue(true);
    }

    @ParallelizationTest(multipleThreadsCount = 3, maxExecutionDurationMultipleForMultipleThreadsExecution = 1.5)
    public void parallelizationTest() throws InterruptedException {
        Thread.sleep(100);
    }

    @LoadTest(maxThreadCount = 3, totalDurationInMilliseconds = 1000)
    public void loadTest() throws InterruptedException {
        System.out.println("Running thread at " + new SimpleDateFormat("HH:mm:ss SS").format(new Date()));
        Thread.sleep(200);
    }

    @LoadTest(
            maxThreadCount = 10,
            totalDurationInMilliseconds = 5000,
            rampUpTimeInMilliseconds = 2000,
            preEmptiveTestClassInstantiationWithTestClassObjectReUsedBetweenIterations = true,
            haltOnError = true,
            abruptTerminationAtTestEnd = true,
            maxExecutionTimeIndividualIteration = 1300)
    public void loadTestWithRampUp() throws InterruptedException {
        System.out.println("Running thread at " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        Thread.sleep(1000);
    }

}
