package core;

import models.Order;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Encapsulated metrics and telemetry tracker for the Order Processing System.
 * Uses atomic primitives for high-concurrency, lock-free statistical aggregation.
 */
public class SystemStatistics {

    private final AtomicInteger totalOrdersSubmitted = new AtomicInteger(0);
    private final AtomicInteger totalOrdersCompleted = new AtomicInteger(0);
    private final AtomicInteger totalOrdersCancelled = new AtomicInteger(0);
    private final AtomicLong totalProcessingDurationMs = new AtomicLong(0);

    private final AtomicInteger kitchenQueueSize = new AtomicInteger(0);
    private final AtomicInteger packagingQueueSize = new AtomicInteger(0);
    private final AtomicInteger deliveryQueueSize = new AtomicInteger(0);

    public void incrementSubmitted() {
        totalOrdersSubmitted.incrementAndGet();
    }

    public void recordCompleted(Order order) {
        totalOrdersCompleted.incrementAndGet();
        totalProcessingDurationMs.addAndGet(order.getTotalProcessingTimeMs());
    }

    public void incrementCancelled() {
        totalOrdersCancelled.incrementAndGet();
    }

    public void updateQueueSizes(int kitchen, int packaging, int delivery) {
        kitchenQueueSize.set(kitchen);
        packagingQueueSize.set(packaging);
        deliveryQueueSize.set(delivery);
    }

    public int getTotalSubmitted() {
        return totalOrdersSubmitted.get();
    }

    public int getTotalCompleted() {
        return totalOrdersCompleted.get();
    }

    public int getTotalCancelled() {
        return totalOrdersCancelled.get();
    }

    public int getActiveOrdersCount() {
        return Math.max(0, totalOrdersSubmitted.get() - totalOrdersCompleted.get() - totalOrdersCancelled.get());
    }

    public double getAverageProcessingTimeSeconds() {
        int completed = totalOrdersCompleted.get();
        if (completed == 0) return 0.0;
        return (totalProcessingDurationMs.get() / (double) completed) / 1000.0;
    }

    public int getKitchenQueueSize() {
        return kitchenQueueSize.get();
    }

    public int getPackagingQueueSize() {
        return packagingQueueSize.get();
    }

    public int getDeliveryQueueSize() {
        return deliveryQueueSize.get();
    }

    public void reset() {
        totalOrdersSubmitted.set(0);
        totalOrdersCompleted.set(0);
        totalOrdersCancelled.set(0);
        totalProcessingDurationMs.set(0);
        kitchenQueueSize.set(0);
        packagingQueueSize.set(0);
        deliveryQueueSize.set(0);
    }
}
