package core;

import models.Order;
import models.OrderStatus;
import threading.DeliveryWorker;
import threading.KitchenWorker;
import threading.PackagingWorker;
import threading.WorkerThread;
import utils.Logger;
import utils.SoundManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OrderProcessingSystem: The Core Orchestrator.
 * Responsibilities:
 * - Manages concurrent pipeline queues using thread-safe PriorityBlockingQueues
 * - Manages the worker threads lifecycle via an ExecutorService
 * - Central registry for active and historical orders
 * - Publishes real-time state events to registered UI listeners
 * - Maintains real-time statistics and throughput telemetry
 */
public class OrderProcessingSystem {

    // Thread-safe queues between processing stages
    // PriorityBlockingQueue ensures VIP orders jump ahead in the queues
    private final BlockingQueue<Order> kitchenQueue = new PriorityBlockingQueue<>();
    private final BlockingQueue<Order> packagingQueue = new PriorityBlockingQueue<>();
    private final BlockingQueue<Order> deliveryQueue = new PriorityBlockingQueue<>();

    // Active in-flight orders mapped by orderId
    private final Map<String, Order> activeOrders = new ConcurrentHashMap<>();

    // Completed orders archive
    private final List<Order> completedOrders = new CopyOnWriteArrayList<>();

    // Registered UI / telemetry listeners
    private final List<OrderUpdateListener> listeners = new CopyOnWriteArrayList<>();

    // Workers list
    private final List<WorkerThread> workers = new CopyOnWriteArrayList<>();
    private ExecutorService executorService;
    private ScheduledExecutorService metricsScheduler;

    // Statistics Tracker
    private final SystemStatistics statistics = new SystemStatistics();

    // Order ID Generator
    private final AtomicInteger orderIdSequence = new AtomicInteger(1000);

    // System state
    private volatile boolean isPaused = false;
    private volatile boolean isRunning = false;

    public OrderProcessingSystem() {
        initializeWorkers();
        startMetricsTimer();
    }

    /**
     * Spawns worker threads within a managed ExecutorService.
     */
    private void initializeWorkers() {
        executorService = Executors.newFixedThreadPool(3, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WorkerPool-" + (counter++));
            }
        });

        KitchenWorker kitchenWorker = new KitchenWorker("Chef-Marco", kitchenQueue, packagingQueue, this);
        PackagingWorker packagingWorker = new PackagingWorker("Packer-Alex", packagingQueue, deliveryQueue, this);
        DeliveryWorker deliveryWorker = new DeliveryWorker("Rider-Dash", deliveryQueue, this);

        workers.add(kitchenWorker);
        workers.add(packagingWorker);
        workers.add(deliveryWorker);

        // Submit to thread pool with explicit thread names
        executorService.submit(new NamedWorkerRunnable(kitchenWorker, "KitchenWorker-1"));
        executorService.submit(new NamedWorkerRunnable(packagingWorker, "PackagingWorker-1"));
        executorService.submit(new NamedWorkerRunnable(deliveryWorker, "DeliveryWorker-1"));

        isRunning = true;
        Logger.system("OrderProcessingSystem started with 3 coordinated worker threads.");
    }

    private static class NamedWorkerRunnable implements Runnable {
        private final Runnable delegate;
        private final String threadName;

        public NamedWorkerRunnable(Runnable delegate, String threadName) {
            this.delegate = delegate;
            this.threadName = threadName;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(threadName);
            delegate.run();
        }
    }

    /**
     * Periodic telemetry ticker to synchronize queue depths and active count with UI.
     */
    private void startMetricsTimer() {
        metricsScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Metrics-Ticker");
            t.setDaemon(true);
            return t;
        });

        metricsScheduler.scheduleAtFixedRate(() -> {
            try {
                statistics.updateQueueSizes(kitchenQueue.size(), packagingQueue.size(), deliveryQueue.size());
                for (OrderUpdateListener listener : listeners) {
                    listener.onQueueMetricsUpdated(
                            kitchenQueue.size(),
                            packagingQueue.size(),
                            deliveryQueue.size(),
                            statistics.getTotalCompleted()
                    );
                }
            } catch (Exception ignored) {
            }
        }, 300, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Generates a new unique Order ID.
     */
    public String generateNextOrderId() {
        return "ORD-" + orderIdSequence.incrementAndGet();
    }

    /**
     * Dispatches a new food order into the kitchen processing pipeline.
     */
    public boolean addOrder(Order order) {
        if (order == null) return false;

        activeOrders.put(order.getOrderId(), order);
        statistics.incrementSubmitted();

        Logger.info(String.format("📥 New Order Enqueued: %s (%s) | Items: %s | VIP: %s",
                order.getOrderId(), order.getCustomerName(), order.getItemsAsString(), order.isVip() ? "YES" : "NO"));

        SoundManager.playOrderAddedSound();

        // Enqueue into kitchen queue
        boolean enqueued = kitchenQueue.offer(order);

        // Notify UI listeners
        for (OrderUpdateListener listener : listeners) {
            listener.onOrderAdded(order);
        }

        return enqueued;
    }

    /**
     * Updates an order's lifecycle status and notifies listeners.
     */
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        for (OrderUpdateListener listener : listeners) {
            listener.onOrderStatusChanged(order, oldStatus, newStatus);
        }
    }

    /**
     * Called when an order reaches final DELIVERED state.
     */
    public void orderCompleted(Order order) {
        activeOrders.remove(order.getOrderId());
        completedOrders.add(0, order); // Most recent first
        statistics.recordCompleted(order);

        SoundManager.playOrderCompletedSound();

        for (OrderUpdateListener listener : listeners) {
            listener.onOrderCompleted(order);
        }
    }

    /**
     * Cancels an order if it is still in the system.
     */
    public boolean cancelOrder(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order != null && !order.getStatus().isTerminal()) {
            kitchenQueue.remove(order);
            packagingQueue.remove(order);
            deliveryQueue.remove(order);

            activeOrders.remove(orderId);
            order.setStatus(OrderStatus.CANCELLED);
            completedOrders.add(0, order);
            statistics.incrementCancelled();

            Logger.warn("🚫 Order cancelled: " + orderId);

            for (OrderUpdateListener listener : listeners) {
                listener.onOrderStatusChanged(order, order.getStatus(), OrderStatus.CANCELLED);
            }
            return true;
        }
        return false;
    }

    /**
     * Broadcasts worker state changes to listeners.
     */
    public void notifyWorkerState(String workerName, String threadState, int processedCount, String currentOrderId) {
        for (OrderUpdateListener listener : listeners) {
            listener.onWorkerStateChanged(workerName, threadState, processedCount, currentOrderId);
        }
    }

    /**
     * Pauses or resumes all worker threads.
     */
    public synchronized void togglePause() {
        if (isPaused) {
            resumeSystem();
        } else {
            pauseSystem();
        }
    }

    public synchronized void pauseSystem() {
        isPaused = true;
        for (WorkerThread worker : workers) {
            worker.pauseWorker();
        }
        Logger.warn("⏸ SYSTEM PAUSED: All worker threads paused at next safe checkpoint.");
    }

    public synchronized void resumeSystem() {
        isPaused = false;
        for (WorkerThread worker : workers) {
            worker.resumeWorker();
        }
        Logger.info("▶ SYSTEM RESUMED: Worker threads active.");
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void clearHistory() {
        completedOrders.clear();
        Logger.info("🧹 Order history cleared.");
    }

    public List<Order> getCompletedOrders() {
        return Collections.unmodifiableList(completedOrders);
    }

    public Map<String, Order> getActiveOrders() {
        return Collections.unmodifiableMap(activeOrders);
    }

    public List<WorkerThread> getWorkers() {
        return Collections.unmodifiableList(workers);
    }

    public SystemStatistics getStatistics() {
        return statistics;
    }

    public void addListener(OrderUpdateListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(OrderUpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Generates a CSV report string of completed orders.
     */
    public String exportHistoryToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID,Customer Name,Items,VIP,Created Time,Completed Time,Total Seconds,Kitchen (s),Packaging (s),Delivery (s),Status\n");
        for (Order o : completedOrders) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,\"%s\"\n",
                    o.getOrderId(),
                    o.getCustomerName().replace("\"", "\"\""),
                    o.getItemsAsString().replace("\"", "\"\""),
                    o.isVip() ? "YES" : "NO",
                    o.getFormattedCreatedTime(),
                    o.getFormattedCompletedTime(),
                    o.getTotalProcessingTimeSeconds(),
                    o.getKitchenDurationMs() / 1000.0,
                    o.getPackagingDurationMs() / 1000.0,
                    o.getDeliveryDurationMs() / 1000.0,
                    o.getStatus().getDisplayName()
            ));
        }
        return sb.toString();
    }

    /**
     * Clean shutdown of the system and threads.
     */
    public void shutdown() {
        Logger.system("Shutting down OrderProcessingSystem...");
        isRunning = false;
        for (WorkerThread worker : workers) {
            worker.shutdown();
        }
        if (metricsScheduler != null) {
            metricsScheduler.shutdownNow();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
