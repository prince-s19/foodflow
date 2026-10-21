package core;

import models.Order;
import models.OrderStatus;

/**
 * Event listener interface for reactive real-time updates from the Order Processing Engine.
 * Follows the Observer pattern to decouple the backend threading logic from the UI view layer.
 */
public interface OrderUpdateListener {

    /**
     * Triggered when a new order is received by the system.
     */
    void onOrderAdded(Order order);

    /**
     * Triggered when an existing order changes its lifecycle status.
     */
    void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);

    /**
     * Triggered when an order successfully reaches the DELIVERED terminal status.
     */
    void onOrderCompleted(Order order);

    /**
     * Triggered when worker state changes (e.g. idle, working, paused) or stats update.
     */
    void onWorkerStateChanged(String workerName, String threadState, int ordersProcessed, String currentOrderId);

    /**
     * Triggered periodically to refresh queue lengths and system-level performance metrics.
     */
    void onQueueMetricsUpdated(int kitchenQueueSize, int packagingQueueSize, int deliveryQueueSize, int totalCompleted);
}
