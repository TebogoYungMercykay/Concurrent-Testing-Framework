package org.assignment;

import org.assignment.annotations.ExecutionMode;
import org.assignment.annotations.Order;
import org.assignment.annotations.RunMode;
import org.assignment.annotations.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomTestRunner extends Runner {

    private final Class<?> testClass;
    private int passedTests = 0;
    private int failedTests = 0;

    public CustomTestRunner(Class<?> testClass) {
        this.testClass = testClass;
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(testClass, "Custom Test Runner Class.");
    }

    @Override
    public void run(RunNotifier notifier) {
        long startTime = System.currentTimeMillis();
        try {
            Object testInstance = testClass.getDeclaredConstructor().newInstance();
            Method[] methods = testClass.getDeclaredMethods();
            List<Method> testMethods = Arrays.asList(methods);

            // Sort methods by @Order annotation
            testMethods.sort(Comparator.comparingInt(m -> {
                Order order = m.getAnnotation(Order.class);
                return order != null ? order.value() : Integer.MAX_VALUE;
            }));

            for (Method method : testMethods) {
                if (method.isAnnotationPresent(Test.class)) {
                    RunMode runMode = method.getAnnotation(RunMode.class);
                    if (runMode != null && runMode.value() == ExecutionMode.CONCURRENT) {
                        runConcurrentTest(method, testInstance, notifier);
                    } else {
                        runSequentialTest(method, testInstance, notifier);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            int totalTests = passedTests + failedTests;

            // ANSI escape codes for colors
            final String ANSI_RESET = "\u001B[0m";
            final String ANSI_GREEN = "\u001B[32m";
            final String ANSI_RED = "\u001B[31m";
            final String ANSI_BLUE = "\u001B[34m";

            System.out.println(ANSI_GREEN + "Total tests: " + totalTests + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Passed tests: " + passedTests + ANSI_RESET);
            System.out.println(ANSI_RED + "Failed tests: " + failedTests + ANSI_RESET);
            System.out.println(ANSI_BLUE + "Total time taken: " + totalTime + " ms" + ANSI_RESET);
        }
    }

    private void runSequentialTest(Method method, Object testInstance, RunNotifier notifier) {
        Description description = Description.createTestDescription(testClass, method.getName());
        notifier.fireTestStarted(description);
        try {
            method.invoke(testInstance);
            passedTests++;
            notifier.fireTestFinished(description);
        } catch (Throwable t) {
            failedTests++;
            notifier.fireTestFailure(new org.junit.runner.notification.Failure(description, t));
        }
    }

    private void runConcurrentTest(Method method, Object testInstance, RunNotifier notifier) {
        Description description = Description.createTestDescription(testClass, method.getName());
        notifier.fireTestStarted(description);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            try {
                passedTests++;
                method.invoke(testInstance);
            } catch (Throwable t) {
                failedTests++;
                notifier.fireTestFailure(new org.junit.runner.notification.Failure(description, t));
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            failedTests++;
            notifier.fireTestFailure(new org.junit.runner.notification.Failure(description, e));
        } finally {
            executor.shutdown();
        }

        notifier.fireTestFinished(description);
    }
}