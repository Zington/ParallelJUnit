# ParallelJUnit - Shifting performance testing left

## Why this exist
Many organizations moving towards DevOps struggle with shifting performance testing left to include it into the CI/CD build pipelines. Many of the types of causes that induces performance or capacity problems cannot be identified in a build pipeline since the infrastructure of the CI-server rarely is production like. However, performance problems from coding mistakes can be identified.

Full-fledged performance tests with a generated distributed load upon a system is seemingly trivial, but is a special skillset that takes years to get good at and decades to master. That's not feasible for developer performance testing. Instead this utility makes it possible to run your unit tests as mini-performance tests to check for concurrency issues and locks - as a complimnet to the built-in performance profilers of most IDEs and to APM tools for finding infrastructure problems.

This enables easy response time assertions over multi-thread tests. 

For **jar download** look here: http://damberg.one/alster/work/paralleljunit/index.html.

For the **C#/.NET** equivalent, look here: https://dev.azure.com/zington/ParallelMsTest

## What it is
It's a custom JUnit test runner that enables executing JUnit test methods in concurrent parallels threads by complimenting JUnit with the use of the *@Test* alternative annotations:

*@ConcurrencyTest* 

*@ParallelizationTest*

*@LoadTest*

## Getting started
Add the following dependency to your maven pom file:

    <dependency>
      <groupId>com.github.zington</groupId>
      <artifactId>ParallelJUnit</artifactId>
      <version>2.0.0</version>
    </dependency>

Or download it as a jar from the link above - or clone this repository and compile it yourself.

## ConcurrencyTest annotation
The **@ConcurrencyTest** is used instead of the **@Test** annotation of regular JUnit. When the **@ConcurrencyTest** is used the test method is executed concurrently in multiple threads. The number of concurrent threads may be set by the **threadCount** argument. Default **threadCount** for ConcurrencyTest is 2.

Regular JUnit **@Test** optional arguments **timeout** and **expected** applies for **@ConcurrencyTest** too.

Comparing @Test annotation with @ConcurrencyTest annotation:

![Test annotation displayed](http://damberg.one/alster/work/paralleljunit/javatest.JPG)

While the @ConcurrencyTest annotation runs the same method in parallel:

![ConcurrencyTest explained](http://damberg.one/alster/work/paralleljunit/javaconcurrencytest.JPG)


## Examples
```java
    import org.junit.Assert;
    import org.junit.Test;
    import org.junit.runner.RunWith;    
    
    //Don't forget to use the adapted JUnit test runner
    @RunWith(ParallelJUnit.class)
    public class Tests {
    
       //Regular JUnit test - passed on to default JUnit runner.
       @Test 
       public void passingRegularTestShouldPass(){
           Assert.assertTrue(true);
       }
       
       //Four parallel concurrent threads, passing timeout (ms)
       @ConcurrencyTest(threadCount = 4, timeout = 200) 
       public void parallelTestShouldPass() throws InterruptedException {
           Assert.assertTrue(true);
           Thread.sleep(100);
           Assert.assertTrue(true);
       }

       //Bad test case since it's fuzzy. Ten parallel threads. Expecting some threads to throw exceptions.
       @ConcurrencyTest(threadCount = 10, expected = TestMethodExecutionException.class)  
       public void exceptionThrowingTestMethodShouldThrowException() throws Exception {
           Assert.assertTrue(true);
           if(Math.random() * 100 < 50){
               throw new Exception("Oups");
           }
           Assert.assertTrue(true);
       }
       
    }
```


## ParallelizationTest
A variant of **ConcurrencyTest** is testing if a tested method seem to be able to handle sequential execution or truly parallel execution. In order to do this in an easy fashion the **@ParallelizationTest** is used. It executes the test method in a single thread first (to avoid first execution initialization problems), then clocks how log the execution of the test method takes with execution with one thread. After this the same method is executed in multiple parallel threads to see if this takes significantly longer than the execution in a single thread. 

![ParallelizationTest explained](http://damberg.one/alster/work/paralleljunit/javaparallelizationtest.JPG)

To summarize the execution sequence:
1. Executing once in one thread as a warm-up (populating caches, ready-compiling components and so forth)
1. Executing once more in a timed execution with one single thread.
1. Executing with multiple concurrent and parallel threads in a timed execution run.
1. Evaluating the success by assessing the ratio between the single thread execution duration with the multiple parallel execution duration variable (using **maxExecutionDurationMultipleForMultipleThreadsExecution** parameter). 

For this type of test the following parameters apply:
* Number of concurrent threads when executed in parallel: **multipleThreadsCount** (default = 3).
* Duration ration to assess test success towards **maxExecutionDurationMultipleForMultipleThreadsExecution** (default = 1.5)

### Example

```java
       //Runs once single-threaded for warm-up, once single-threaded for benchmark, and once multi-threaded for comparison. 
       @ParallelizationTest(multipleThreadsCount = 3, maxExecutionDurationMultipleForMultipleThreadsExecution = 1.5)
       public void parallelizationTest() throws InterruptedException {
           Thread.sleep(100);
       }
```


## LoadTest
This annotation is for performance testing closer to LoadRunner/JMeter or equivalent tools. It enables ramp-up of load and holding a system under load for a longer period of time.
The unit test method runs in concurrent parallel threads as with the other test types in this library, and the execution time for each individual method execution (for each iteration) can be assessed towards a set threshold.
Using this test type the thread pool used is filled up again with a new execution when a test method execution is finished.

![LoadTest explained](http://damberg.one/alster/work/paralleljunit/javaloadtest.JPG)

### Examples

```java
       //Runs this test method continuously in a thread pool of 3 concurrent threads for 1000 milliseconds 
        @LoadTest(maxThreadCount = 3, totalDurationInMilliseconds = 1000)
        public void loadTest() throws InterruptedException {
            System.out.println("Running thread at " + new SimpleDateFormat("HH:mm:ss SS").format(new Date()));
            Thread.sleep(200);
        }

        //Run this test method continuously in 10 parallel, but ramp these up evenly over 2 seconds 
        //of time until full load and halt on any test interation taking longer than 1300 milliseconds and re-use
        //the test class instances for method invocation. Halt test abruptly after 5 seconds.
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
```

### Options/parameters/arguments:
* maxThreadCount (default 2)
* rampUpTimeInMilliseconds (default 0)
* preEmptiveTestClassInstantiationWithTestClassObjectReUsedBetweenIterations (default false, saves time from test class instantiation in each iteration)
* totalDurationInMilliseconds (default 3000)
* haltOnError (default false, makes the test halt upon errors - including execution time assertions)
* maxExecutionTimeIndividualIteration (default ignored, throws an assertion error if any of the method executions takes longer than this)
* abruptTerminationAtTestEnd (default true, if set to false it leaves all threads up to 30 seconds to finish);
* timeout (default 30000, halts test abruptly if it takes longer than this, for compatibility with JUnit @Test annotation)
* expected (any expected exception to ignore)

## Technical notes
* Each test method thread execution is executed on its own test class instance.
* Throws **TestDurationCheckException** if test execution takes longer than the given timeout.
* Throws **TestClassInstantiationException** if test class cannot be instantiated with default parameter-less constructor.
* Throws **TestMethodExecutionException** if any of the executed threads throws any exception (collected as inner exceptions).
* Default **expected** (Expected thrown exceptions from test execution) = none.

## Testing notes
Testing with the same data over and over is a bit risky. In part because any cache in the system might respond after first request, giving un-normal response times, and partly because many databases uses read locks for requested records. Accessing the same record many times hence could create queues in the database. 

Testing in a dev environment is not in any way equal of testing in a more production like environment. An SQL triggering a full table scan is way different with limited data in the database, and an environment with indexes maintained and refreshed is way quicker than any environment that's remotely neglected.

Developer performance tests can only be used to make sure the code **CAN** get good capacity/performance. There are numerous more obstacles due to environmental factos, infrastructural factors, data-setup (data volume and distribution), other things happening in the system at the same time (batch jobs, other requests). But it is relevant making sure the system code **can** deliver. That helps narrowing down any performance related issues identified - and hopefully you yourself can come out without blame. 
