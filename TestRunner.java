import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRunner {
    private final ThreadPool threadPool; // ThreadPool for executing tests concurrently
    private final List<Method> testMethods = new ArrayList<>(); // List to store discovered test methods
    private final Map<String, Boolean> testResults = new ConcurrentHashMap<>(); // Map to store test results by method name
    private final AtomicInteger passedCount = new AtomicInteger(); // Counter for passed tests
    private final AtomicInteger failedCount = new AtomicInteger(); // Counter for failed tests
    private final AtomicInteger skippedCount = new AtomicInteger(); // Counter for skipped tests

    // Constructor to initialize the TestRunner with a specific number of threads
    public TestRunner(int numberOfThreads) {
        this.threadPool = new ThreadPool(numberOfThreads); // Create a ThreadPool with the specified number of threads
    }

    // Discover and run tests in the specified test class
    public void runTests(Class<?> testClass) {
        long startTime = System.currentTimeMillis(); // Record the start time

        // Discover methods annotated with @Test
        discoverTestMethods(testClass);

        // Sort methods based on their order specified in the @Order annotation
        testMethods.sort(Comparator.comparingInt(m -> {
            Annotation.Order order = m.getAnnotation(Annotation.Order.class);
            return order != null ? order.value() : Integer.MAX_VALUE; // Use MAX_VALUE for methods without @Order
        }));

        // Use a CountDownLatch to wait for all test execution tasks to complete
        CountDownLatch latch = new CountDownLatch(testMethods.size());

        // Submit each test method to the thread pool for execution
        for (Method method : testMethods) {
            threadPool.submit(() -> {
                try {
                    executeTestMethod(method, testClass); // Execute the test method
                } finally {
                    latch.countDown(); // Decrement the latch count when a task finishes
                }
            });
        }

        // Wait for all tasks to complete before shutting down the thread pool
        try {
            latch.await(); // Wait for all tests to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }

        // Shutdown the thread pool after all tasks are submitted
        threadPool.shutdown();

        // Print a summary of the test results
        printSummary(startTime);
    }

    // Discover methods annotated with @Test in the specified class
    private void discoverTestMethods(Class<?> testClass) {
        for (Method method : testClass.getDeclaredMethods()) { // Iterate over declared methods
            if (method.isAnnotationPresent(Annotation.Test.class)) { // Check if the method has @Test annotation
                testMethods.add(method); // Add the method to the list of test methods
            }
        }
    }

    // Execute a test method with retry logic
    private void executeTestMethod(Method method, Class<?> testClass) {
        int maxRetries = 3; // Maximum number of retries for a failed test
        int attempt = 0; // Counter for the number of attempts
        boolean success = false; // Flag to indicate if the test was successful

        while (attempt < maxRetries && !success) { // Retry until max attempts or success
            try {
                attempt++; // Increment the attempt counter
                // Check if the test should be skipped based on dependencies
                if (shouldSkipTest(method)) {
                    skippedCount.incrementAndGet(); // Increment the skipped tests count
                    testResults.put(method.getName(), false); // Mark the test as skipped
                    System.out.println(method.getName() + ": SKIPPED"); // Print skip message
                    return; // Exit the method
                }

                // Run the test method
                Object testInstance = testClass.getDeclaredConstructor().newInstance(); // Create an instance of the test class
                method.setAccessible(true); // Allow access to the method
                long startTime = System.currentTimeMillis(); // Record start time for execution
                method.invoke(testInstance); // Invoke the test method
                long executionTime = System.currentTimeMillis() - startTime; // Calculate execution time

                // Mark the test as passed
                passedCount.incrementAndGet(); // Increment the passed tests count
                testResults.put(method.getName(), true); // Mark the test as passed
                System.out.printf("%s:(Execution time: %dms)%n", method.getName(), executionTime); // Print execution details
                success = true; // Set success flag to true to exit retry loop

            } catch (InvocationTargetException e) { // Handle exceptions thrown by the test method
                Throwable cause = e.getCause(); // Get the root cause of the exception
                if (cause instanceof RuntimeException) {
                    System.err.println(method.getName() + ": FAILED (Attempt " + attempt + "): " + cause.getMessage()); // Print failure message
                    if (attempt >= maxRetries) {
                        // Mark the test as failed after max retries
                        failedCount.incrementAndGet(); // Increment the failed tests count
                        testResults.put(method.getName(), false); // Mark the test as failed
                        System.out.println("Test failed after " + maxRetries + " attempts: " + method.getName()); // Print final failure message
                    }
                } else {
                    // Print error message for unexpected exceptions
                    System.err.println(method.getName() + ": ERROR during execution: " + e.getMessage());
                }
            } catch (Exception e) {
                // Handle other exceptions if needed
                System.err.println(method.getName() + ": ERROR during execution: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for unexpected exceptions
            }
        }
    }

    // Determine if a test should be skipped based on dependencies
    private boolean shouldSkipTest(Method method) {
        if (method.isAnnotationPresent(Annotation.DependsOn.class)) { // Check for @DependsOn annotation
            for (String dependency : method.getAnnotation(Annotation.DependsOn.class).value()) { // Iterate through dependencies
                Boolean result = testResults.get(dependency); // Get the result of the dependency
                if (result == null || !result) {
                    // If the dependency was skipped or failed, skip this test
                    return true; // Indicate the test should be skipped
                }
            }
        }
        return false; // Return false if no dependencies are unmet
    }

    // Print a summary of the test results after all tests have been run
    private void printSummary(long startTime) {
        long endTime = System.currentTimeMillis(); // Record the end time
        long totalTime = endTime - startTime; // Calculate total execution time

        // Print total execution time and counts of test results
        System.out.println("\nTotal Execution Time: " + totalTime + " ms");
        System.out.println("Tests passed: " + passedCount.get());
        System.out.println("Tests failed: " + failedCount.get());
        System.out.println("Tests skipped: " + skippedCount.get());

        // Calculate speedup and efficiency metrics
        double speedup = (double) totalTime / (passedCount.get() + failedCount.get() + skippedCount.get());
        double efficiency = (speedup / (12 * 1000)) * 100; // Example calculations, adjust as needed

        // Print speedup and efficiency results
        System.out.printf("Speedup: %.2fx%n", speedup);
        System.out.printf("Efficiency: %.2f%%%n", efficiency);
    }
}
