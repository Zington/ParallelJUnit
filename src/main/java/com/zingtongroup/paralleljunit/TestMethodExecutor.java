package com.zingtongroup.paralleljunit;

import java.lang.reflect.Method;

class TestMethodExecutor implements Runnable {

    Object testClassObject;
    Method method;
    Exception innerException;
    Object testMethodReturnObject;

    @Override
    public void run() {
        try {
            CustomTestMethodRunnerBase.runBeforeMethods(testClassObject);
            testMethodReturnObject = method.invoke(testClassObject);
            CustomTestMethodRunnerBase.runAfterMethods(testClassObject);
        } catch (Exception e) {
            innerException = e;
        }
    }

    TestMethodExecutor(Object testClassObject, Method testMethod){
        this.testClassObject = testClassObject;
        this.method = testMethod;
    }
}
