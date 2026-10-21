package threading;

import core.OrderProcessingSystem;
import models.Order;
import models.OrderStatus;
import utils.Logger;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * PackagingWorker Thread:
 * Simulates food packaging and quality check stage.
 * Consumes prepared orders from packaging queue, packs food in neobrutal boxes (1-3 sec simulated delay),
 * transitions order status from PREPARED -> PACKAGING -> PACKAGED, and enqueues to the delivery queue.
 */
public class PackagingWorker implements WorkerThread {

    private final String workerName;
    private final BlockingQueue<Order> inputQueue;
    private final BlockingQueue<Order> outputQueue;
    private final OrderProcessingSystem system;
    private final Random random = new Random();

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private volatile String threadState = "IDLE";
    private volatile int ordersProcessed = 0;
    private volatile Order currentOrder = null;

    public PackagingWorker(String workerName, BlockingQueue<Order> inputQueue, BlockingQueue<Order> outputQueue, OrderProcessingSystem system) {
        this.workerName = workerName;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.system = system;
    }

    @Override
    public void run() {
        Logger.worker(String.format("[%s] Thread initialized and ready for packaging orders.", workerName));

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                synchronized (pauseLock) {
                    while (paused && running) {
                        threadState = "PAUSED";
                        system.notifyWorkerState(workerName, threadState, ordersProcessed, null);
                        pauseLock.wait();
                    }
                }

                if (!running) break;

                threadState = "IDLE";
                currentOrder = null;
                system.notifyWorkerState(workerName, threadState, ordersProcessed, null);

                // Poll from packaging queue
                Order order = inputQueue.poll(500, TimeUnit.MILLISECONDS);
                if (order == null) {
                    continue;
                }

                currentOrder = order;
                threadState = "PACKAGING";
                order.setCurrentWorkerName(workerName);

                // Packaging delay: 1000 - 3000 ms
                int minDelay = order.isVip() ? 1000 : 1200;
                int maxDelay = order.isVip() ? 2000 : 3000;
                int packTimeMs = minDelay + random.nextInt(maxDelay - minDelay + 1);
                order.setEstimatedDurationMs(packTimeMs);

                // State transition: PREPARED -> PACKAGING
                system.updateOrderStatus(order, OrderStatus.PACKAGING);
                Logger.worker(String.format("[%s] Boxing & sealing %s (%s). Packaging time: %.2fs",
                        workerName, order.getOrderId(), order.getCustomerName(), packTimeMs / 1000.0));
                system.notifyWorkerState(workerName, "PACKAGING", ordersProcessed, order.getOrderId());

                // Simulated packaging loop with 100ms ticks
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < packTimeMs) {
                    synchronized (pauseLock) {
                        while (paused && running) {
                            threadState = "PAUSED";
                            system.notifyWorkerState(workerName, threadState, ordersProcessed, order.getOrderId());
                            pauseLock.wait();
                        }
                    }
                    if (!running) break;
                    Thread.sleep(Math.min(100, packTimeMs - (System.currentTimeMillis() - startTime)));
                }

                if (!running) break;

                long elapsedMs = System.currentTimeMillis() - startTime;
                order.setPackagingDurationMs(elapsedMs);

                // State transition: PACKAGING -> PACKAGED
                system.updateOrderStatus(order, OrderStatus.PACKAGED);
                Logger.success(String.format("[%s] Packaged and sealed %s in %.2fs. Passing to Delivery Queue.",
                        workerName, order.getOrderId(), elapsedMs / 1000.0));

                ordersProcessed++;

                // Thread-safe handoff to delivery queue
                outputQueue.put(order);

            } catch (InterruptedException e) {
                Logger.warn(String.format("[%s] Worker interrupted. Shutting down gracefully.", workerName));
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.error(String.format("[%s] Error occurred during packaging: %s", workerName, e.getMessage()));
            }
        }

        threadState = "STOPPED";
        currentOrder = null;
        system.notifyWorkerState(workerName, threadState, ordersProcessed, null);
        Logger.worker(String.format("[%s] Thread terminated. Total orders packaged: %d", workerName, ordersProcessed));
    }

    @Override
    public String getWorkerName() {
        return workerName;
    }

    @Override
    public String getThreadState() {
        return threadState;
    }

    @Override
    public int getOrdersProcessed() {
        return ordersProcessed;
    }

    @Override
    public Order getCurrentOrder() {
        return currentOrder;
    }

    @Override
    public void pauseWorker() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    @Override
    public void resumeWorker() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void shutdown() {
        running = false;
        resumeWorker();
    }
}
