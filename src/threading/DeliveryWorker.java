package threading;

import core.OrderProcessingSystem;
import models.Order;
import models.OrderStatus;
import utils.Logger;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * DeliveryWorker Thread:
 * Simulates the final dispatch and delivery stage.
 * Consumes packaged orders from delivery queue, simulates courier transit (1-2 sec simulated delay),
 * transitions order status from PACKAGED -> DELIVERING -> DELIVERED, records completion metrics,
 * and notifies the system and UI of final delivery.
 */
public class DeliveryWorker implements WorkerThread {

    private final String workerName;
    private final BlockingQueue<Order> inputQueue;
    private final OrderProcessingSystem system;
    private final Random random = new Random();

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private volatile String threadState = "IDLE";
    private volatile int ordersProcessed = 0;
    private volatile Order currentOrder = null;

    public DeliveryWorker(String workerName, BlockingQueue<Order> inputQueue, OrderProcessingSystem system) {
        this.workerName = workerName;
        this.inputQueue = inputQueue;
        this.system = system;
    }

    @Override
    public void run() {
        Logger.worker(String.format("[%s] Thread initialized and ready for delivery dispatches.", workerName));

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

                // Poll from delivery queue
                Order order = inputQueue.poll(500, TimeUnit.MILLISECONDS);
                if (order == null) {
                    continue;
                }

                currentOrder = order;
                threadState = "DELIVERING";
                order.setCurrentWorkerName(workerName);

                // Delivery delay: 1000 - 2000 ms
                int minDelay = order.isVip() ? 800 : 1000;
                int maxDelay = order.isVip() ? 1500 : 2000;
                int deliveryTimeMs = minDelay + random.nextInt(maxDelay - minDelay + 1);
                order.setEstimatedDurationMs(deliveryTimeMs);

                // State transition: PACKAGED -> DELIVERING
                system.updateOrderStatus(order, OrderStatus.DELIVERING);
                Logger.worker(String.format("[%s] Dispatching courier for %s (%s). Estimated transit: %.2fs",
                        workerName, order.getOrderId(), order.getCustomerName(), deliveryTimeMs / 1000.0));
                system.notifyWorkerState(workerName, "DELIVERING", ordersProcessed, order.getOrderId());

                // Simulated delivery transit loop with 100ms ticks
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < deliveryTimeMs) {
                    synchronized (pauseLock) {
                        while (paused && running) {
                            threadState = "PAUSED";
                            system.notifyWorkerState(workerName, threadState, ordersProcessed, order.getOrderId());
                            pauseLock.wait();
                        }
                    }
                    if (!running) break;
                    Thread.sleep(Math.min(100, deliveryTimeMs - (System.currentTimeMillis() - startTime)));
                }

                if (!running) break;

                long elapsedMs = System.currentTimeMillis() - startTime;
                order.setDeliveryDurationMs(elapsedMs);

                // State transition: DELIVERING -> DELIVERED
                system.updateOrderStatus(order, OrderStatus.DELIVERED);
                ordersProcessed++;

                Logger.success(String.format("🎉 [%s] ORDER DELIVERED! %s for %s completed in %.2fs total (Kitchen: %.1fs, Pack: %.1fs, Delivery: %.1fs)",
                        workerName,
                        order.getOrderId(),
                        order.getCustomerName(),
                        order.getTotalProcessingTimeSeconds(),
                        order.getKitchenDurationMs() / 1000.0,
                        order.getPackagingDurationMs() / 1000.0,
                        order.getDeliveryDurationMs() / 1000.0));

                // Notify final order completion
                system.orderCompleted(order);

            } catch (InterruptedException e) {
                Logger.warn(String.format("[%s] Worker interrupted. Shutting down gracefully.", workerName));
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.error(String.format("[%s] Error occurred during delivery: %s", workerName, e.getMessage()));
            }
        }

        threadState = "STOPPED";
        currentOrder = null;
        system.notifyWorkerState(workerName, threadState, ordersProcessed, null);
        Logger.worker(String.format("[%s] Thread terminated. Total orders delivered: %d", workerName, ordersProcessed));
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
