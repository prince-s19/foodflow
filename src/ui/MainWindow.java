package ui;

import core.OrderProcessingSystem;
import core.OrderUpdateListener;
import models.Order;
import models.OrderStatus;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main Application Window for FoodFlow - Online Food Order Processing Simulator.
 * Features a pure Neobrutalism UI/UX theme, event-driven reactive architecture,
 * and multi-threaded concurrency visualization.
 */
public class MainWindow extends JFrame implements OrderUpdateListener {

    private final OrderProcessingSystem system;

    // UI Sub-components
    private JLabel activeOrdersBadge;
    private JLabel systemStatusBadge;
    private StatsPanel statsPanel;
    private DashboardLanePanel kitchenLane;
    private DashboardLanePanel packagingLane;
    private DashboardLanePanel deliveryLane;
    private ThreadMonitorPanel threadMonitorPanel;
    private OrderInputPanel inputPanel;
    private HistoryTablePanel historyPanel;
    private LogConsolePanel logConsolePanel;

    // Smooth UI timer for continuous card animation and elapsed seconds ticker
    private Timer uiAnimationTimer;

    public MainWindow(OrderProcessingSystem system) {
        this.system = system;

        setTitle("FOODFLOW // Online Food Order Processing Simulator [Neobrutalism Edition]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        // System Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        buildRootLayout();

        // Register listener with backend
        system.addListener(this);

        // Start UI animation timer (100ms interval for smooth progress bar transitions)
        startAnimationTimer();

        // Handle clean window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAnimationTimer();
                system.shutdown();
            }
        });
    }

    private void buildRootLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 8));
        rootPanel.setBackground(NeobrutalismTheme.COLOR_BG);
        rootPanel.setBorder(new EmptyBorder(8, 10, 10, 10));

        // 1. Top Header & Metrics Banner
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(buildHeaderBar());
        topContainer.add(Box.createVerticalStrut(6));

        statsPanel = new StatsPanel();
        topContainer.add(statsPanel);

        rootPanel.add(topContainer, BorderLayout.NORTH);

        // 2. Center & Body Layout
        // Split into Left (Dashboard Lanes + Thread Monitor) and Right (Input + History / Logs tabs)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(780);
        mainSplitPane.setResizeWeight(0.60);
        mainSplitPane.setDividerSize(6);
        mainSplitPane.setBorder(null);
        mainSplitPane.setOpaque(false);

        // Left Component: 3-Lane Dashboard (Top) + Thread Monitor (Bottom)
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setOpaque(false);

        leftPanel.add(buildThreeLaneDashboard(), BorderLayout.CENTER);

        threadMonitorPanel = new ThreadMonitorPanel();
        threadMonitorPanel.setPreferredSize(new Dimension(780, 142));
        leftPanel.add(threadMonitorPanel, BorderLayout.SOUTH);

        mainSplitPane.setLeftComponent(leftPanel);

        // Right Component: Order Dispatch Form (Top) + Tabbed Panel (History & Logs)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);

        inputPanel = new OrderInputPanel(system);
        inputPanel.setPreferredSize(new Dimension(460, 315));
        rightPanel.add(inputPanel, BorderLayout.NORTH);

        // Bottom Right Tabs: Completed Orders Table & Live Logs Terminal
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        tabbedPane.setBackground(NeobrutalismTheme.COLOR_WHITE);
        tabbedPane.setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        historyPanel = new HistoryTablePanel(system);
        logConsolePanel = new LogConsolePanel();

        tabbedPane.addTab("[AUDIT] COMPLETED ORDERS", historyPanel);
        tabbedPane.addTab("[LOGS] THREAD TERMINAL", logConsolePanel);

        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        mainSplitPane.setRightComponent(rightPanel);

        rootPanel.add(mainSplitPane, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel buildHeaderBar() {
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(NeobrutalismTheme.COLOR_WHITE);
        headerPanel.setBorder(new NeobrutalismTheme.HardShadowBorder(3, 4, 8));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoBox = new JLabel("FOODFLOW");
        logoBox.setFont(NeobrutalismTheme.FONT_TITLE);
        logoBox.setOpaque(true);
        logoBox.setBackground(NeobrutalismTheme.ACCENT_YELLOW);
        logoBox.setForeground(NeobrutalismTheme.COLOR_BLACK);
        logoBox.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false),
                new EmptyBorder(4, 10, 4, 10)
        ));
        brandPanel.add(logoBox);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel subTitle = new JLabel("FOOD ORDER PROCESSOR // MULTI-THREADED PIPELINE");
        subTitle.setFont(NeobrutalismTheme.FONT_HEADER);
        subTitle.setForeground(NeobrutalismTheme.COLOR_BLACK);

        JLabel techInfo = new JLabel("OOPS Thread Synchronization: BlockingQueue<Order> | ExecutorService | Event-Driven UI");
        techInfo.setFont(NeobrutalismTheme.FONT_SMALL);
        techInfo.setForeground(new Color(0x55, 0x55, 0x55));

        titles.add(subTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(techInfo);

        brandPanel.add(titles);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Right Status Badges
        JPanel statusBadgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusBadgePanel.setOpaque(false);

        activeOrdersBadge = new JLabel("ACTIVE ORDERS: 0");
        activeOrdersBadge.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        activeOrdersBadge.setOpaque(true);
        activeOrdersBadge.setBackground(NeobrutalismTheme.COLOR_BLACK);
        activeOrdersBadge.setForeground(NeobrutalismTheme.COLOR_WHITE);
        activeOrdersBadge.setBorder(new EmptyBorder(6, 12, 6, 12));
        statusBadgePanel.add(activeOrdersBadge);

        systemStatusBadge = new JLabel("STATUS: RUNNING");
        systemStatusBadge.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        systemStatusBadge.setOpaque(true);
        systemStatusBadge.setBackground(NeobrutalismTheme.ACCENT_GREEN);
        systemStatusBadge.setForeground(NeobrutalismTheme.COLOR_BLACK);
        systemStatusBadge.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(5, 10, 5, 10)
        ));
        statusBadgePanel.add(systemStatusBadge);

        headerPanel.add(statusBadgePanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildThreeLaneDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 3, 10, 0));
        dashboard.setOpaque(false);

        kitchenLane = new DashboardLanePanel(
                "KITCHEN LANE",
                "[KITCHEN]",
                NeobrutalismTheme.ACCENT_BLUE,
                this::handleOrderCancel
        );

        packagingLane = new DashboardLanePanel(
                "PACKAGING LANE",
                "[PACKAGING]",
                NeobrutalismTheme.ACCENT_YELLOW,
                this::handleOrderCancel
        );

        deliveryLane = new DashboardLanePanel(
                "DELIVERY LANE",
                "[DELIVERY]",
                NeobrutalismTheme.ACCENT_GREEN,
                this::handleOrderCancel
        );

        dashboard.add(kitchenLane);
        dashboard.add(packagingLane);
        dashboard.add(deliveryLane);

        return dashboard;
    }

    private void handleOrderCancel(String orderId) {
        system.cancelOrder(orderId);
    }

    private void startAnimationTimer() {
        uiAnimationTimer = new Timer(100, e -> {
            kitchenLane.refreshCards();
            packagingLane.refreshCards();
            deliveryLane.refreshCards();
            statsPanel.updateStats(system.getStatistics());
        });
        uiAnimationTimer.start();
    }

    private void stopAnimationTimer() {
        if (uiAnimationTimer != null && uiAnimationTimer.isRunning()) {
            uiAnimationTimer.stop();
        }
    }

    // --- OrderUpdateListener Callbacks (Thread-safe via SwingUtilities) ---

    @Override
    public void onOrderAdded(Order order) {
        SwingUtilities.invokeLater(() -> {
            kitchenLane.addOrderCard(order);
            updateHeaderBadges();
        });
    }

    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        SwingUtilities.invokeLater(() -> {
            // Remove from previous lane if stage transitioned
            if (newStatus.isInPackaging()) {
                kitchenLane.removeOrderCard(order.getOrderId());
                packagingLane.addOrderCard(order);
            } else if (newStatus.isInDelivery()) {
                kitchenLane.removeOrderCard(order.getOrderId());
                packagingLane.removeOrderCard(order.getOrderId());
                deliveryLane.addOrderCard(order);
            } else if (newStatus.isTerminal()) {
                kitchenLane.removeOrderCard(order.getOrderId());
                packagingLane.removeOrderCard(order.getOrderId());
                deliveryLane.removeOrderCard(order.getOrderId());
                historyPanel.reloadOrders();
            }

            kitchenLane.refreshCards();
            packagingLane.refreshCards();
            deliveryLane.refreshCards();
            updateHeaderBadges();
        });
    }

    @Override
    public void onOrderCompleted(Order order) {
        SwingUtilities.invokeLater(() -> {
            deliveryLane.removeOrderCard(order.getOrderId());
            historyPanel.reloadOrders();
            updateHeaderBadges();
        });
    }

    @Override
    public void onWorkerStateChanged(String workerName, String threadState, int ordersProcessed, String currentOrderId) {
        SwingUtilities.invokeLater(() -> {
            threadMonitorPanel.updateWorkerState(workerName, threadState, ordersProcessed, currentOrderId);

            String statusSnippet = String.format("%s: %s%s",
                    workerName,
                    threadState,
                    currentOrderId != null ? " (" + currentOrderId + ")" : "");

            if (workerName.contains("Kitchen") || workerName.contains("Marco")) {
                kitchenLane.updateWorkerStatus(statusSnippet);
            } else if (workerName.contains("Packaging") || workerName.contains("Alex")) {
                packagingLane.updateWorkerStatus(statusSnippet);
            } else if (workerName.contains("Delivery") || workerName.contains("Dash")) {
                deliveryLane.updateWorkerStatus(statusSnippet);
            }

            if (system.isPaused()) {
                systemStatusBadge.setText("STATUS: PAUSED");
                systemStatusBadge.setBackground(NeobrutalismTheme.ACCENT_RED);
                systemStatusBadge.setForeground(NeobrutalismTheme.COLOR_WHITE);
            } else {
                systemStatusBadge.setText("STATUS: RUNNING");
                systemStatusBadge.setBackground(NeobrutalismTheme.ACCENT_GREEN);
                systemStatusBadge.setForeground(NeobrutalismTheme.COLOR_BLACK);
            }
        });
    }

    @Override
    public void onQueueMetricsUpdated(int kitchenQueueSize, int packagingQueueSize, int deliveryQueueSize, int totalCompleted) {
        SwingUtilities.invokeLater(() -> {
            statsPanel.updateStats(system.getStatistics());
            updateHeaderBadges();
        });
    }

    private void updateHeaderBadges() {
        int activeCount = system.getStatistics().getActiveOrdersCount();
        activeOrdersBadge.setText("ACTIVE ORDERS: " + activeCount);
    }
}
