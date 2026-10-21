package threading;

import core.OrderProcessingSystem;
import models.Order;
import models.OrderStatus;
import utils.Logger;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * KitchenWorker Thread:
 * Simulates food preparation stage.
 * Consumes orders from the incoming kitchen queue, prepares the items (2-5 sec simulated delay),
 * transitions order status from PENDING -> PREPARING -> PREPARED, and enqueues to the packaging queue.
 */
public class KitchenWorker implements WorkerThread {

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

    public KitchenWorker(String workerName, BlockingQueue<Order> inputQueue, BlockingQueue<Order> outputQueue, OrderProcessingSystem system) {
        this.workerName = workerName;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.system = system;
    }

    @Override
    public void run() {
        Logger.worker(String.format("[%s] Thread initialized and ready for cooking orders.", workerName));

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Check if worker is paused
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

                // Poll order with timeout so worker can respond to shutdown or pause
                Order order = inputQueue.poll(500, TimeUnit.MILLISECONDS);
                if (order == null) {
                    continue;
                }

                // Pick up order
                currentOrder = order;
                threadState = "PREPARING";
                order.setCurrentWorkerName(workerName);

                // VIP orders prepare slightly faster (2000-3500ms) vs Regular (2500-5000ms)
                int minDelay = order.isVip() ? 2000 : 2500;
                int maxDelay = order.isVip() ? 3500 : 5000;
                int prepTimeMs = minDelay + random.nextInt(maxDelay - minDelay + 1);
                order.setEstimatedDurationMs(prepTimeMs);

                // State transition: PENDING -> PREPARING
                system.updateOrderStatus(order, OrderStatus.PREPARING);
                Logger.worker(String.format("[%s] Started preparing %s (%s) with %d items. Prep time: %.2fs",
                        workerName, order.getOrderId(), order.getCustomerName(), order.getItems().size(), prepTimeMs / 1000.0));
                system.notifyWorkerState(workerName, "PREPARING", ordersProcessed, order.getOrderId());

                // Simulated cooking loop in 100ms ticks to allow progress bar animation and responsiveness
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < prepTimeMs) {
                    synchronized (pauseLock) {
                        while (paused && running) {
                            threadState = "PAUSED";
                            system.notifyWorkerState(workerName, threadState, ordersProcessed, order.getOrderId());
                            pauseLock.wait();
                        }
                    }
                    if (!running) break;
                    Thread.sleep(Math.min(100, prepTimeMs - (System.currentTimeMillis() - startTime)));
                }

                if (!running) break;

                long elapsedMs = System.currentTimeMillis() - startTime;
                order.setKitchenDurationMs(elapsedMs);

                // State transition: PREPARING -> PREPARED
                system.updateOrderStatus(order, OrderStatus.PREPARED);
                Logger.success(String.format("[%s] Completed cooking for %s in %.2fs. Passing to Packaging Queue.",
                        workerName, order.getOrderId(), elapsedMs / 1000.0));

                ordersProcessed++;

                // Thread-safe handoff to packaging queue
                outputQueue.put(order);

            } catch (InterruptedException e) {
                Logger.warn(String.format("[%s] Worker interrupted. Shutting down gracefully.", workerName));
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.error(String.format("[%s] Error occurred during preparation: %s", workerName, e.getMessage()));
            }
        }

        threadState = "STOPPED";
        currentOrder = null;
        system.notifyWorkerState(workerName, threadState, ordersProcessed, null);
        Logger.worker(String.format("[%s] Thread terminated. Total orders cooked: %d", workerName, ordersProcessed));
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
