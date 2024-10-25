package org.assignment;

import org.assignment.annotations.ExecutionMode;
import org.assignment.annotations.Order;
import org.assignment.annotations.RunMode;
import org.assignment.annotations.Test;

import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(CustomTestRunner.class)
public class TestRunner {

    @Test
    @Order(1)
    public void someLibraryMethodReturnsTrue() {
        ListSynchronization classUnderTest = new ListSynchronization();
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.simpleTestMethod());
    }

    @Test
    @Order(2)
    @RunMode(ExecutionMode.SEQUENTIAL)
    public void sequentialLibraryTest() {
        ListSynchronization classUnderTest = new ListSynchronization();
        int iterations = 10; 
        AtomicBoolean hasFailed = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            if (!classUnderTest.add(5000)) {
                hasFailed.set(true);
            }
        }
        long endTime = System.nanoTime();

        double executionTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Sequential test completed in %.2f ms%n", executionTime);

        assertFalse("Sequential test should not have any failures", hasFailed.get());
        assertTrue("Sequential test should complete in a reasonable time", executionTime < 60000); 
    }

    @Test
    @Order(3)
    @RunMode(ExecutionMode.SEQUENTIAL)
    public void sequentialLibraryTestShortDelay() {
        ListSynchronization classUnderTest = new ListSynchronization();
        int iterations = 10;
        AtomicBoolean hasFailed = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            if (!classUnderTest.update(1000)) { 
                hasFailed.set(true);
            }
        }
        long endTime = System.nanoTime();

        double executionTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Sequential test with short delay completed in %.2f ms%n", executionTime);

        assertFalse("Sequential test with short delay should not have any failures", hasFailed.get());
        assertTrue("Sequential test with short delay should complete in a reasonable time", executionTime < 60000); 
    }

    @Test
    @Order(4)
    @RunMode(ExecutionMode.SEQUENTIAL)
    public void sequentialLibraryTestMediumDelay() {
        ListSynchronization classUnderTest = new ListSynchronization();
        int iterations = 10;
        AtomicBoolean hasFailed = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            if (!classUnderTest.delete(3000)) { 
                hasFailed.set(true);
            }
        }
        long endTime = System.nanoTime();

        double executionTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Sequential test with medium delay completed in %.2f ms%n", executionTime);

        assertFalse("Sequential test with medium delay should not have any failures", hasFailed.get());
        assertTrue("Sequential test with medium delay should complete in a reasonable time", executionTime < 60000); 
    }

    @Test
    @Order(5)
    @RunMode(ExecutionMode.CONCURRENT)
    public void concurrentLibraryTestMediumDelay() throws InterruptedException {
        ListSynchronization classUnderTest = new ListSynchronization();
        int iterations = 10;
        CountDownLatch latch = new CountDownLatch(iterations);
        ThreadPool threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors());
        AtomicBoolean hasFailed = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            threadPool.submitTask(() -> {
                try {
                    if (!classUnderTest.add(5000)) { 
                        hasFailed.set(true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        threadPool.shutdown();
        long endTime = System.nanoTime();

        double executionTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Concurrent test with medium delay and %d threads completed in %.2f ms%n", Runtime.getRuntime().availableProcessors(), executionTime);

        assertFalse("Concurrent test with medium delay should not have any failures", hasFailed.get());
        assertTrue("Concurrent test with medium delay should complete in a reasonable time", executionTime < 60000); 
    }

    @Test
    @Order(6)
    @RunMode(ExecutionMode.CONCURRENT)
    public void concurrentLibraryTestShortDelay() throws InterruptedException {
        ListSynchronization classUnderTest = new ListSynchronization();
        int iterations = 10;
        CountDownLatch latch = new CountDownLatch(iterations);
        ThreadPool threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors());
        AtomicBoolean hasFailed = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            threadPool.submitTask(() -> {
                try {
                    if (!classUnderTest.update(1000)) { 
                        hasFailed.set(true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        threadPool.shutdown();
        long endTime = System.nanoTime();

        double executionTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Concurrent test with short delay and %d threads completed in %.2f ms%n", Runtime.getRuntime().availableProcessors(), executionTime);

        assertFalse("Concurrent test with short delay should not have any failures", hasFailed.get());
        assertTrue("Concurrent test with short delay should complete in a reasonable time", executionTime < 60000); 
    }

    @Test
    @Order(7)
    @RunMode(ExecutionMode.CONCURRENT)
    public void concurrentLibraryTest() throws InterruptedException {
        ListSynchronization classUnderTest = new ListSynchronization();
        int iterations = 10; 
        CountDownLatch latch = new CountDownLatch(iterations);
        ThreadPool threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors());
        AtomicBoolean hasFailed = new AtomicBoolean(false);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            threadPool.submitTask(() -> {
                try {
                    if (!classUnderTest.delete(3000)) {
                        hasFailed.set(true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        threadPool.shutdown();
        long endTime = System.nanoTime();

        double executionTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Concurrent test with %d threads completed in %.2f ms%n", Runtime.getRuntime().availableProcessors(), executionTime);

        assertFalse("Concurrent test should not have any failures", hasFailed.get());
        assertTrue("Concurrent test should complete in a reasonable time", executionTime < 60000); 
    }
}
