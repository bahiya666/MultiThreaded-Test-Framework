import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool { 
    private final int numberOfThreads; // Number of threads in the pool
    private final Worker[] workers; // Array to hold the worker threads
    private final BlockingQueue<Runnable> taskQueue; // Queue for tasks to be executed
    private volatile boolean isShutdown = false; // Flag to indicate if the pool is shutting down

    // Constructor to initialize the thread pool with a specific number of threads
    public ThreadPool(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        taskQueue = new LinkedBlockingQueue<>(); // Initialize the task queue
        workers = new Worker[numberOfThreads]; // Create the worker array

        // Create and start worker threads
        for (int i = 0; i < numberOfThreads; i++) {
            workers[i] = new Worker(); // Create a new Worker instance
            new Thread(workers[i], "Worker-" + i).start(); // Start the worker thread
        }
    }

    // Method to submit a new task to the thread pool
    public void submit(Runnable task) {
        if (!isShutdown) { // Check if the pool is not shut down
            taskQueue.offer(task); // Add the task to the queue
        }
    }

    // Method to shut down the thread pool
    public void shutdown() {
        isShutdown = true; // Set shutdown flag to true
        for (Worker worker : workers) {
            worker.stopWorker(); // Stop each worker thread
        }
    }

    // Inner class representing a worker thread
    private class Worker implements Runnable {
        private volatile boolean running = true; // Flag to control the worker's running state

        // Method that defines the worker's task execution
        @Override
        public void run() {
            while (running) { // Keep running until told to stop
                try {
                    Runnable task = taskQueue.take(); // Wait for a task to become available
                    task.run(); // Execute the task
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }
        }

        // Method to stop the worker thread
        public void stopWorker() {
            running = false; // Set running flag to false
        }
    }
}
