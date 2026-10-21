package threading;

import models.Order;

/**
 * Common contract for worker threads in the pipeline.
 * Exposes inspection metrics for the Neobrutalism Thread Status Monitor.
 */
public interface WorkerThread extends Runnable {

    String getWorkerName();

    String getThreadState();

    int getOrdersProcessed();

    Order getCurrentOrder();

    void pauseWorker();

    void resumeWorker();

    boolean isPaused();

    void shutdown();
}
