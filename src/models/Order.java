package models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing a Food Order within the simulation pipeline.
 * Adheres to strict Object-Oriented Programming (OOPS) principles:
 * - Full encapsulation with private fields and guarded accessors
 * - Defensive copying of mutable collections (items list)
 * - Immutability where appropriate (orderId, createdTimestamp)
 * - Meaningful equals, hashCode, and toString overrides
 */
public class Order implements Comparable<Order> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String orderId;
    private final String customerName;
    private final List<String> items;
    private final long createdTimestamp;
    private final boolean isVip;

    private volatile OrderStatus status;
    private volatile long completedTimestamp;
    private volatile long stageStartTimestamp;
    private volatile long estimatedDurationMs;
    private volatile String currentWorkerName = "Unassigned";

    // Detailed stage duration tracking in milliseconds
    private volatile long kitchenDurationMs = 0;
    private volatile long packagingDurationMs = 0;
    private volatile long deliveryDurationMs = 0;

    /**
     * Primary Constructor for creating a new Order.
     *
     * @param orderId       Unique identifier for the order (e.g., "ORD-1001")
     * @param customerName  Name of the customer placing the order
     * @param items         List of food items ordered (defensively copied)
     * @param isVip         Whether this order has priority processing status
     */
    public Order(String orderId, String customerName, List<String> items, boolean isVip) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty.");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty.");
        }
        this.orderId = orderId.trim();
        this.customerName = customerName.trim();
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.isVip = isVip;
        this.createdTimestamp = System.currentTimeMillis();
        this.stageStartTimestamp = this.createdTimestamp;
        this.status = OrderStatus.PENDING;
        this.completedTimestamp = 0;
    }

    public Order(String orderId, String customerName, List<String> items) {
        this(orderId, customerName, items, false);
    }

    // --- Encapsulated Getters & Setters ---

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns an unmodifiable defensive copy of the items list.
     */
    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getItemsAsString() {
        return String.join(", ", items);
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public boolean isVip() {
        return isVip;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Thread-safe status update with stage timestamp transition.
     */
    public synchronized void setStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.stageStartTimestamp = System.currentTimeMillis();
        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
            this.completedTimestamp = this.stageStartTimestamp;
        }
    }

    public long getStageStartTimestamp() {
        return stageStartTimestamp;
    }

    public void setStageStartTimestamp(long stageStartTimestamp) {
        this.stageStartTimestamp = stageStartTimestamp;
    }

    public long getEstimatedDurationMs() {
        return estimatedDurationMs;
    }

    public void setEstimatedDurationMs(long estimatedDurationMs) {
        this.estimatedDurationMs = estimatedDurationMs;
    }

    public String getCurrentWorkerName() {
        return currentWorkerName;
    }

    public void setCurrentWorkerName(String currentWorkerName) {
        this.currentWorkerName = currentWorkerName != null ? currentWorkerName : "Unassigned";
    }

    public long getKitchenDurationMs() {
        return kitchenDurationMs;
    }

    public void setKitchenDurationMs(long kitchenDurationMs) {
        this.kitchenDurationMs = kitchenDurationMs;
    }

    public long getPackagingDurationMs() {
        return packagingDurationMs;
    }

    public void setPackagingDurationMs(long packagingDurationMs) {
        this.packagingDurationMs = packagingDurationMs;
    }

    public long getDeliveryDurationMs() {
        return deliveryDurationMs;
    }

    public void setDeliveryDurationMs(long deliveryDurationMs) {
        this.deliveryDurationMs = deliveryDurationMs;
    }

    public long getTotalProcessingTimeMs() {
        if (completedTimestamp > 0) {
            return completedTimestamp - createdTimestamp;
        }
        return System.currentTimeMillis() - createdTimestamp;
    }

    public double getTotalProcessingTimeSeconds() {
        return getTotalProcessingTimeMs() / 1000.0;
    }

    public long getStageElapsedMs() {
        return Math.max(0, System.currentTimeMillis() - stageStartTimestamp);
    }

    public String getFormattedCreatedTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(createdTimestamp), ZoneId.systemDefault())
                .format(TIME_FORMATTER);
    }

    public String getFormattedCompletedTime() {
        if (completedTimestamp <= 0) return "--:--:--";
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(completedTimestamp), ZoneId.systemDefault())
                .format(TIME_FORMATTER);
    }

    /**
     * Computes progress from 0.0 to 1.0 within current stage based on elapsed time vs estimated duration.
     */
    public double getStageProgress() {
        if (status.isTerminal()) return 1.0;
        if (estimatedDurationMs <= 0) return 0.0;
        double progress = (double) getStageElapsedMs() / (double) estimatedDurationMs;
        return Math.min(1.0, Math.max(0.0, progress));
    }

    /**
     * Comparable implementation: VIP orders are prioritized over standard orders.
     * Within the same priority level, FIFO (earlier createdTimestamp) is preserved.
     */
    @Override
    public int compareTo(Order other) {
        if (this.isVip != other.isVip) {
            return this.isVip ? -1 : 1; // VIP comes first
        }
        return Long.compare(this.createdTimestamp, other.createdTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return String.format("Order[%s | %s | %s | Items: %d | Status: %s | Time: %.1fs]",
                orderId,
                isVip ? "VIP" : "REG",
                customerName,
                items.size(),
                status.getDisplayName(),
                getTotalProcessingTimeSeconds());
    }
}
