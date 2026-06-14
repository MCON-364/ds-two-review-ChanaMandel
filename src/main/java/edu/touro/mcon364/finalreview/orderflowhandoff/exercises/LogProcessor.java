package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.LogLevel;
import edu.touro.mcon364.finalreview.model.LogMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LogProcessor.
 * <p>
 * A server receives log messages from different parts of an application:
 * authentication, payments, reporting, background jobs, and so on. Messages may
 * arrive while earlier messages are still being processed. We want one part of
 * the program to submit log messages, and a small group of worker threads to
 * process those messages in the background.
 * <p>
 * This class represents that log-processing service.
 * <p>
 * The main problem you are solving:
 * - incoming messages need to wait somewhere until a worker is ready for them;
 * - more than one worker may be running at the same time;
 * - every submitted message should be processed once;
 * - while messages are processed, the class must keep accurate summary counts.
 * <p>
 * Requirements:
 * - submit(message) accepts one log message for later processing.
 * - start(workerCount) starts exactly workerCount background workers.
 * - workerCount must be positive.
 * - workers should keep processing while the processor is still accepting work
 * or while there is still unprocessed work waiting.
 * - stop() tells the processor to stop accepting/expecting more work and waits
 * until the already-submitted work has been handled.
 * - getTotalProcessed() returns how many log messages have been processed.
 * - getCountsByLevel() returns how many processed messages there were for each
 * LogLevel.
 * - getCountsByLevel() must not allow callers to mutate this class's internal
 * state.
 * - The class must behave correctly when multiple threads interact with it.
 * <p>
 * Questions to think about before coding:
 * - Where should submitted messages wait before a worker processes them? - Linked Blocking Queue
 * - What behavior do we need from that structure: newest first, oldest first,
 * priority order, or something else? oldest first FIFO
 * - Which state is shared by multiple threads? the totals - summary counts
 * - Which operations must be protected so the statistics stay correct?
 * - How will worker threads know when to continue waiting for work and when to
 * finish? blocking queue
 * - What should happen if stop() is called while messages are still waiting? await shutdown
 * - What should the public getter methods return so outside code cannot damage
 * the processor's internal state? return a copy
 */
public class LogProcessor {

    /*
     * Decide what fields this class needs.
     *
     * Think about:
     * - pending work
     * - worker threads
     * - whether the processor is still running
     * - total processed count
     * - count by log level
     */
    private final BlockingQueue<LogMessage> waitingQueue = new LinkedBlockingQueue<>();
    private final List<Thread> workers = new ArrayList<>();
    private final ConcurrentHashMap<LogLevel, AtomicInteger> countByLevel = new ConcurrentHashMap<>();
    private final AtomicInteger totalProcessed = new AtomicInteger();
    private volatile boolean running = false;

    /**
     * Accept one message for processing.
     */
    public void submit(LogMessage message) {
        // TODO: implement
//        if (running) {
//            waitingQueue.offer(message);
//        }
        /**
         * Accept one message for processing.
         */

        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        // Check if the processor is active before accepting work
        if (!running) {
            throw new IllegalStateException("Processor is not running and cannot accept new messages");
        }

        try {
            // put() blocks if the queue happens to be full (if bounded),
            // ensuring we don't silently drop logs under high load.
            waitingQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("Thread was interrupted while submitting message", e);
        }
    }

    /**
     * Start the requested number of background workers. - create new pool with executor service
     */
    public void start(int workerCount) {
        // TODO: implement

        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be positive: " + workerCount);
        }

        // Ensure we don't start the processor twice or reset workers while running
        if (running) {
            throw new IllegalStateException("Processor is already running");
        }

        running = true;

        for (int i = 0; i < workerCount; i++) {
            // Create a new thread passing the workerLoop method as the Runnable task
            Thread worker = new Thread(this::workerLoop, "LogProcessor-Worker-" + i);
            workers.add(worker);
            worker.start();
        }
    }

    /**
     * The work done by one background worker.
     * <p>
     * You may keep this helper method, rename it, or replace it with another
     * private helper if your design is clearer that way.
     */
    private void workerLoop() {
        // TODO: implement
        // Workers should keep running while the processor is accepting work
        // OR while there is still work in the queue to be drained.
        while (running || !waitingQueue.isEmpty()) {
            try {
                // Use poll with a timeout so threads don't hang forever
                // if 'running' becomes false while the queue is empty.
                LogMessage message = waitingQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);

                if (message != null) {
                    process(message);
                }
            } catch (InterruptedException e) {
                // Restore interrupted status and exit the loop if interrupted
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Process one message and update whatever statistics this class tracks.
     */
    private void process(LogMessage message) {
        // TODO: implement
        // 1. Update the total count of all processed messages
        totalProcessed.incrementAndGet();

        // 2. Safely get the log level from the message
        LogLevel level = message.level();
        if (level != null) {
            // computeIfAbsent ensures that if a LogLevel hasn't been seen yet,
            // we atomically initialize it with a new AtomicInteger(0).
            countByLevel.computeIfAbsent(level, k -> new AtomicInteger(0))
                    .incrementAndGet();
        }
    }

    /**
     * Stop the processor and wait for worker threads to finish.
     */
    public void stop() throws InterruptedException {
        // TODO: implement
        // 1. Signal workers that we are no longer accepting new work
        running = false;

        // 2. Wait for every worker thread to finish executing its loop
        for (Thread worker : workers) {
            // join() blocks the calling thread until this specific worker thread terminates
            worker.join();
        }

        // Clear the workers list now that they are all dead
        workers.clear();
    }

    /**
     * Return the number of messages processed so far.
     */
    public int getTotalProcessed() {
        // TODO: implement
        return totalProcessed.get();
    }

    /**
     * Return a safe snapshot of the counts by level.
     */
    public Map<LogLevel, Integer> getCountsByLevel() {
        // TODO: implement
        return countByLevel.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                ));
    }
}
