package com.zingtongroup.paralleljunit;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.Method;

/**
 * Custom JUnit runner for parallel execution of JUnit test methods.
 */
public class ParallelJUnit extends Runner {

    private final Class<?> testClass;

    public ParallelJUnit(Class<?> testClass) {
        super();
        this.testClass = testClass;
    }

    @Override
    public Description getDescription() {
        return Description
                .createTestDescription(testClass, "JUnit with parallel test execution possibilities with '@Perf()' annotation.");
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            for (Method method : testClass.getMethods()) {
                if(method.isAnnotationPresent(ConcurrencyTest.class)){
                    new ParallelTestRunner(notifier, testClass, method).run();
                }

                if(method.isAnnotationPresent(ParallelizationTest.class)){
                    new ParallelizationTestRunner(notifier, testClass, method).run();
                }

                if(method.isAnnotationPresent(LoadTest.class)){
                    new LoadTestMethodRunner(notifier, testClass, method).run();
                }

                if (method.isAnnotationPresent(Test.class)) {
                    Test test = method.getAnnotation(Test.class);
                    Class<? extends Throwable> expectedException = test.expected();
                    Object testObject = testClass.getDeclaredConstructor().newInstance();
                    notifier.fireTestStarted(Description
                            .createTestDescription(testClass, method.getName()));
                    try{
                        method.invoke(testObject);
                    }catch (Exception e){
                        if(!e.getClass().equals(expectedException))
                            notifier.fireTestFailure(new Failure(Description.createTestDescription(testClass, method.getName()), new TestMethodExecutionException(e)));
                    }
                    notifier.fireTestFinished(Description
                            .createTestDescription(testClass, method.getName()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}