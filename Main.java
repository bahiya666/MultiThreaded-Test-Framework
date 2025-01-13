// File: Main.java
public class Main {
    public static void main(String[] args) {
        int numberOfThreads = 4; // Number of threads for the thread pool

        System.out.println("Running tests sequentially:");
        SequentialTestRunner sequentialTestRunner = new SequentialTestRunner();
        sequentialTestRunner.runTests(SampleTests.class);

        System.out.println("\nRunning tests using multi-threaded framework:");
        TestRunner multiThreadedTestRunner = new TestRunner(numberOfThreads);
        multiThreadedTestRunner.runTests(SampleTests.class);
    }
}
