package com.zingtongroup.paralleljunit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(ParallelJUnit.class)
public class LoadTestTests {

    long startTime;
    long executionTime;

    @Before
    public void setup(){
        startTime = System.currentTimeMillis();
    }

    @After
    public void teardown(){
        executionTime = System.currentTimeMillis() - startTime;
        System.out.println("Execution time: " + executionTime + " ms.");
        Assert.assertFalse(executionTime < 5000 && executionTime >= 3000);
    }

    @LoadTest
    public void allDefaultsShouldWork() throws InterruptedException {
        Thread.sleep(500);
    }
}
