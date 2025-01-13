Multi-Threaded Test Framework
This Java project implements a testing framework with the ability to run tests sequentially or concurrently using multi-threading. It supports custom annotations for test management, including prioritization (@Order), dependencies (@DependsOn), and basic retries.

Features
Custom Annotations:

@Test: Marks methods as test methods.
@Order: Defines the execution order of tests (optional).
@DependsOn: Marks dependencies between tests to ensure one test runs only if its dependencies succeed.
Test Execution:

Sequential Execution: Runs tests one by one in the order defined by @Order.
Concurrent Execution: Uses a thread pool to execute tests concurrently, speeding up the overall test execution.
Retry Mechanism: Supports retrying failed tests up to a defined number of attempts.

Metrics: Provides performance metrics such as total execution time, passed/failed/skipped tests, speedup, and efficiency.

Usage
1. Annotations
You can use the following annotations in your test classes to define test methods, their order, and dependencies.
2. Test Class Example
Create a test class with methods annotated with @Test, @Order, and @DependsOn.
3. Running Tests
You can run tests either sequentially or using multiple threads by calling the appropriate test runner.
  Sequential Test Runner
  This will run tests one by one in the order specified by the @Order annotation.
  Multi-Threaded Test Runner
  This uses a thread pool to run tests concurrently. You can specify the number of threads in the pool.
4. Thread Pool
The framework uses a custom ThreadPool implementation for concurrent execution of tests. You can adjust the number of threads based on your requirements.
