import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SequentialTestRunner {
    private final List<Method> testMethods = new ArrayList<>();
    private final AtomicInteger passedCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();
    private final AtomicInteger skippedCount = new AtomicInteger();

    // Discover and run tests sequentially
    public void runTests(Class<?> testClass) {
        long startTime = System.currentTimeMillis();

        // Discover methods annotated with @Test
        discoverTestMethods(testClass);

        // Sort methods based on @Order annotation
        testMethods.sort(Comparator.comparingInt(method -> {
            Annotation.Order order = method.getAnnotation(Annotation.Order.class);
            return order != null ? order.value() : Integer.MAX_VALUE;
        }));

        // Execute each test sequentially
        for (Method method : testMethods) {
            executeTestMethod(method, testClass);
        }

        // Print the summary
        printSummary(startTime);
    }

    // Discover methods annotated with @Test
    private void discoverTestMethods(Class<?> testClass) {
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Annotation.Test.class)) {
                testMethods.add(method);
            }
        }
    }

    // Execute a test method
    private void executeTestMethod(Method method, Class<?> testClass) {
        try {
            // Check if the test should be skipped based on dependencies
            if (shouldSkipTest(method)) {
                skippedCount.incrementAndGet();
                return;
            }

            // Run the test method
            Object testInstance = testClass.getDeclaredConstructor().newInstance();
            method.setAccessible(true);
            method.invoke(testInstance);

            // Mark the test as passed
            passedCount.incrementAndGet();
        } catch (Exception e) {
            // Mark the test as failed
            failedCount.incrementAndGet();
        }
    }

    // Check if a method is annotated with @DependsOn and determine if it should be skipped
    private boolean shouldSkipTest(Method method) {
        Annotation.DependsOn dependsOn = method.getAnnotation(Annotation.DependsOn.class);
        if (dependsOn != null) {
            for (String dependency : dependsOn.value()) {
                boolean dependencyPassed = testMethods.stream()
                        .filter(m -> m.getName().equals(dependency))
                        .anyMatch(m -> m.isAnnotationPresent(Annotation.Test.class));
                if (!dependencyPassed) {
                    return true;
                }
            }
        }
        return false;
    }

    // Print the summary of test results
    private void printSummary(long startTime) {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("Sequential Execution Time: " + totalTime + " ms");
        System.out.println("Number of tests passed: " + passedCount.get());
        System.out.println("Number of tests failed: " + failedCount.get());
        System.out.println("Number of tests skipped: " + skippedCount.get());
    }
}
