package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.SensorReading;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Homework 2 — Sensor reading processor.
 *
 * A monitoring system receives readings from sensors over time. One part of the
 * program submits readings as they arrive. Another part of the program processes
 * those readings using one or more background workers.
 *
 * This class is responsible for coordinating that handoff and for keeping a
 * summary of the readings that were actually processed.
 *
 * The important question is not only "How do we calculate the stats?" It is also:
 * "What happens when readings are being submitted and processed by different
 * threads at the same time?"
 *
 * Requirements:
 * - submit(reading) accepts one new sensor reading for later processing.
 * - start(workerCount) starts workerCount background workers.
 * - workerCount must be greater than 0.
 * - Workers should process submitted readings until the processor is stopped and
 *   all already-submitted readings have been handled.
 * - stop() tells the processor to stop accepting/processing future work and waits
 *   until the workers finish the remaining work.
 * - getTotalProcessed() returns how many readings have been processed so far.
 * - getStats() returns summary statistics for the processed reading values:
 *   count, minimum, maximum, sum, and average.
 * - Public reporting methods must not expose mutable internal state.
 *
 * Before coding, think about:
 * - Which object or objects represent work waiting to be processed? a queue
 * - Which object or objects represent work that has already been processed? a list
 * - Which state can be accessed by more than one thread? atomic
 * - How will workers know when to keep working and when to stop? when the first queue is empty and jobs are finished being processed
 * - What should happen if getStats() is called while workers are still running? they should wait
 * - Is it better to store all processed readings and calculate stats later, or
 *   update numeric summary state as each reading is processed? as each reading is processed
 * - If several workers update the same stats, how will those updates stay correct? using atomics
 */
public class SensorProcessor {
    // 1. The Handoff (Producer-Consumer)
    private final BlockingQueue<SensorReading> queue = new LinkedBlockingQueue<>();
    private final List<Thread> workers = new ArrayList<>();
    private volatile boolean running = false;
    private ExecutorService executor;

    // 2. The Metrics
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

    // 3. The Guard
    private final Object statsLock = new Object(); // Our "lock" for the stats object
    /**
     * Accept one sensor reading for processing.
     *
     * @param reading the reading to process later
     */
    public void submit(SensorReading reading) {
        // TODO: decide where submitted readings should be stored
        if (reading == null) {
            throw new IllegalArgumentException("Sensor reading cannot be null");
        }

        // Guard clause: Only accept work if the processor is active
        if (!running) {
            throw new IllegalStateException("Processor is not running and cannot accept readings");
        }

        try {
            // put() blocks the thread if the queue happens to be full,
            // preventing us from overwhelming system memory under heavy load.
            queue.put(reading);
        } catch (InterruptedException e) {
            // Restore the interrupted status so higher-level code knows it was interrupted
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Start background workers that process submitted readings.
     *
     * @param workerCount number of worker threads to start
     * @throws IllegalArgumentException if workerCount is not positive
     */
    public void start(int workerCount) {
        // TODO: validate workerCount
        // TODO: start the requested number of workers
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount must be greater than 0");
        }

        // Prevent starting the processor if it's already active
        if (running) {
            throw new IllegalStateException("Processor has already been started");
        }

        // Set the lifecycle flag so workers know they are allowed to run
        running = true;

        // Create and launch the worker threads
        for (int i = 0; i < workerCount; i++) {
            // Each thread is given the workerLoop method as its execution body
            Thread worker = new Thread(this::workerLoop, "SensorWorker-" + i);
            workers.add(worker);
            worker.start();
        }
    }

    /**
     * Logic run by each worker.
     *
     * This method is private because callers should not run worker logic directly.
     * The worker should repeatedly look for work, process it when available, and
     * eventually exit when the processor is stopping and no work remains.
     */
    private void workerLoop() {
        // TODO: implement the worker behavior
        // The worker stays alive if we are "running"
        // OR if we've stopped but the queue still has readings to finish.
        while (running || !queue.isEmpty()) {
            try {
                // poll() with a timeout is better than take() here.
                // It prevents the thread from hanging forever if 'running' becomes false.
                SensorReading reading = queue.poll(100, TimeUnit.MILLISECONDS);

                if (reading != null) {
                    // 1. Increment the atomic counter (no lock needed)
                    totalProcessed.incrementAndGet();

                    // 2. Update the complex stats (requires our lock)
                    synchronized (statsLock) {
                        stats.accept(reading.value());
                    }
                }
            } catch (InterruptedException e) {
                // If the thread is interrupted, we exit the loop and end the thread.
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Stop the processor and wait for workers to finish.
     *
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public void stop() throws InterruptedException {
        // TODO: signal that work should stop
        // TODO: wait for all workers to finish
        // 1. Signal that we are no longer accepting new work
        running = false;

        // 2. Wait for each worker thread to finish its work loop
        for (Thread worker : workers) {
            // join() blocks the current thread until the 'worker' thread dies
            worker.join();
        }

        // Clear the list so start() could potentially be called again
        workers.clear();
    }

    /**
     * Return the number of readings processed so far.
     */
    public int getTotalProcessed() {
        // TODO: return the processed count safely
        return totalProcessed.get();
    }

    /**
     * Return summary statistics for the processed reading values.
     *
     * If no readings have been processed yet, return an empty
     * DoubleSummaryStatistics object.
     */
    public DoubleSummaryStatistics getStats() {
        // TODO: calculate or return the current statistics safely
        synchronized (statsLock) {
            // We return a copy/snapshot so the caller can't
            // accidentally modify our internal state.
            DoubleSummaryStatistics snapshot = new DoubleSummaryStatistics();
            snapshot.combine(stats);
            return snapshot;
        }
    }
}
