package com.zingtongroup.paralleljunit;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

class LoadTestMethodRunner extends CustomTestMethodRunnerBase {
    ExecutorService threadPool;
    LoadTest loadTestInstance;
    public boolean isInterrupted;
    Set<Object> testClassInstances;

    LoadTestMethodRunner(RunNotifier notifier, Class<?> testClass, Method method) throws Exception {
        super(notifier, testClass, method);
        testClassInstances = new HashSet<>();
        loadTestInstance = method.getAnnotation(LoadTest.class);
        if(loadTestInstance == null) throw new Exception("Test method annotation is not @LoadTest.");

        threadPool = Executors.newFixedThreadPool(loadTestInstance.maxThreadCount());
        System.out.println("Running test method " + method.getName() + " as load test.");
    }



    synchronized void setInterrupted(){
        isInterrupted = true;
    }

    void rampUpAndRun() {
        //If no ramp-up
        if (loadTestInstance.rampUpTimeInMilliseconds() <= 0 || loadTestInstance.maxThreadCount() < 2) {
            for(int i = 0; i < loadTestInstance.maxThreadCount(); i++){
                Object testClassInstance = null;
                if(loadTestInstance.preEmptiveTestClassInstantiationWithTestClassObjectReUsedBetweenIterations())
                    testClassInstance = createTestClassInstance();
                startContinuousMethodExecutionThread(testClassInstance);
            }
            return;
        }

        //If ramp-up
        long delayBetweenThreadStarts = loadTestInstance.rampUpTimeInMilliseconds() / (loadTestInstance.maxThreadCount() - 1);
        Object testClassInstance = null;
        if(loadTestInstance.preEmptiveTestClassInstantiationWithTestClassObjectReUsedBetweenIterations())
            testClassInstance = createTestClassInstance();
        this.startContinuousMethodExecutionThread(testClassInstance); //First execution thread start without delay;
        Timer timer = new Timer("Test method execution thread delayed start timer");
        for(int i = 0; i < loadTestInstance.maxThreadCount() - 1; i++){
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Object testClassInstance = null;
                    if(loadTestInstance.preEmptiveTestClassInstantiationWithTestClassObjectReUsedBetweenIterations())
                        testClassInstance = createTestClassInstance();
                    startContinuousMethodExecutionThread(testClassInstance);
                }
            }, delayBetweenThreadStarts * i);
        }
    }

    void shutdownThreads(){
        if(!loadTestInstance.abruptTerminationAtTestEnd()){
            try {
                threadPool.awaitTermination(30, TimeUnit.SECONDS);
                threadPool.shutdown();
            } catch (InterruptedException e) {
                setInterrupted();
            }
        } else {
            threadPool.shutdownNow();
        }
    }

    void setShutdownTimer(){
        new Timer("Load test method thread pool shutdown timer")
                .schedule(new TimerTask() {
            @Override
            public void run() {
                shutdownThreads();
            }
        }, loadTestInstance.totalDurationInMilliseconds());
    }

    @Override
    void run(){
        notifier.fireTestStarted(Description
                .createTestDescription(testClass, method.getName()));

        setShutdownTimer();
        rampUpAndRun();

        notifier.fireTestFinished(Description.createTestDescription(testClass, method.getName()));
    }

    class MethodExecution implements Runnable{

        private Object testClassInstance;

        MethodExecution(Object testClassInstance){
            this.testClassInstance = testClassInstance;
            isInterrupted = false;
        }

        @Override
        public void run() {
            if(isInterrupted)return;
            boolean testClassInstanceIsLocal = (testClassInstance == null);
            if(testClassInstanceIsLocal)
                testClassInstance = createTestClassInstance();
            try {
                runBeforeMethods(testClassInstance);
                method.invoke(testClassInstance);
                runAfterMethods(testClassInstance);
                startContinuousMethodExecutionThread(testClassInstance);
            } catch (Exception e) {
                innerExceptions.add(new TestMethodExecutionException(e));
                if(loadTestInstance.haltOnError()) setInterrupted();
            }
        }
    }

    void startContinuousMethodExecutionThread(Object testClassInstance)  {
        if(isInterrupted)return;
        MethodExecution methodExecution = new MethodExecution(testClassInstance);
        Thread executionThread = new Thread(methodExecution);
        try{
            threadPool.submit(executionThread);
        }catch (RejectedExecutionException e){
            //Thread pool has been shut down due to timeout. Ignored.
        }
        long startTime = System.currentTimeMillis();
        executionThread.start();
        try {
            executionThread.join();
        } catch (InterruptedException e) {
            setInterrupted();
            return;
        }
        long iterationTime = System.currentTimeMillis() - startTime;
        if(loadTestInstance.maxExecutionTimeIndividualIteration() > 0 &&
                iterationTime > loadTestInstance.maxExecutionTimeIndividualIteration()){
            if(isInterrupted)return;
            if(loadTestInstance.haltOnError()) setInterrupted();
            notifier.fireTestFailure(
                    new Failure(
                            Description.createTestDescription(testClass, method.getName()),
                            new TestMethodExecutionDurationCheckFailedException(
                                    "Test method execution iteration took " +
                                            iterationTime +
                                            " ms while the maximum iteration time was " +
                                            loadTestInstance.maxExecutionTimeIndividualIteration() +
                                            " ms.")
                    )
            );
        }
    }
}
