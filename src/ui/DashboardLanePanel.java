package ui;

import models.Order;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vertical Processing Lane representing one stage of the pipeline:
 * - KITCHEN LANE (Pending & Cooking)
 * - PACKAGING LANE (Boxing & Sealing)
 * - DELIVERY LANE (Courier Transit)
 */
public class DashboardLanePanel extends JPanel {

    private final String stageName;
    private final String stageTag;
    private final Color accentColor;
    private final Consumer<String> onCancelRequested;

    private JLabel queueCountBadge;
    private JLabel workerStatusLabel;
    private JPanel cardsContainer;
    private JLabel emptyStateLabel;

    private final Map<String, OrderCard> activeCardMap = new LinkedHashMap<>();

    public DashboardLanePanel(String stageName, String stageTag, Color accentColor, Consumer<String> onCancelRequested) {
        this.stageName = stageName;
        this.stageTag = stageTag;
        this.accentColor = accentColor;
        this.onCancelRequested = onCancelRequested;

        setLayout(new BorderLayout(0, 0));
        setBackground(NeobrutalismTheme.COLOR_PANEL_BG);
        setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        buildHeader();
        buildCardsArea();
    }

    private void buildHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(accentColor);
        headerPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Top Header Row: Stage Tag + Name + Count Badge
        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        JLabel titleLabel = new JLabel(stageName);
        titleLabel.setFont(NeobrutalismTheme.FONT_HEADER);
        titleLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        topRow.add(titleLabel, BorderLayout.WEST);

        queueCountBadge = new JLabel("0 ORDERS");
        queueCountBadge.setFont(NeobrutalismTheme.FONT_SMALL);
        queueCountBadge.setOpaque(true);
        queueCountBadge.setBackground(NeobrutalismTheme.COLOR_BLACK);
        queueCountBadge.setForeground(NeobrutalismTheme.COLOR_WHITE);
        queueCountBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        topRow.add(queueCountBadge, BorderLayout.EAST);

        headerPanel.add(topRow);
        headerPanel.add(Box.createVerticalStrut(4));

        // Bottom Header Row: Worker Status Indicator
        JPanel workerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        workerRow.setOpaque(false);

        workerStatusLabel = new JLabel("WORKER: IDLE");
        workerStatusLabel.setFont(NeobrutalismTheme.FONT_SMALL);
        workerStatusLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        workerRow.add(workerStatusLabel);

        headerPanel.add(workerRow);

        // Solid 3px separator line under header
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(100, 3));
        separator.setBackground(NeobrutalismTheme.COLOR_BLACK);

        JPanel combinedHeader = new JPanel(new BorderLayout());
        combinedHeader.add(headerPanel, BorderLayout.CENTER);
        combinedHeader.add(separator, BorderLayout.SOUTH);

        add(combinedHeader, BorderLayout.NORTH);
    }

    private void buildCardsArea() {
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(NeobrutalismTheme.COLOR_BG);
        cardsContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        emptyStateLabel = new JLabel("<html><center><b>LANE IDLE</b><br><small>Waiting for incoming orders</small></center></html>", SwingConstants.CENTER);
        emptyStateLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        emptyStateLabel.setForeground(new Color(0x88, 0x88, 0x88));
        emptyStateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyStateLabel.setBorder(new EmptyBorder(40, 10, 40, 10));

        cardsContainer.add(emptyStateLabel);

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    public synchronized void addOrderCard(Order order) {
        if (!activeCardMap.containsKey(order.getOrderId())) {
            OrderCard card = new OrderCard(order, onCancelRequested);
            activeCardMap.put(order.getOrderId(), card);
            rebuildCardsView();
        }
    }

    public synchronized void removeOrderCard(String orderId) {
        if (activeCardMap.remove(orderId) != null) {
            rebuildCardsView();
        }
    }

    public synchronized void refreshCards() {
        for (OrderCard card : activeCardMap.values()) {
            card.updateDisplay();
        }
    }

    public synchronized void clearLane() {
        activeCardMap.clear();
        rebuildCardsView();
    }

    public void updateWorkerStatus(String statusText) {
        workerStatusLabel.setText(statusText);
    }

    private synchronized void rebuildCardsView() {
        cardsContainer.removeAll();

        if (activeCardMap.isEmpty()) {
            cardsContainer.add(Box.createVerticalGlue());
            cardsContainer.add(emptyStateLabel);
            cardsContainer.add(Box.createVerticalGlue());
            queueCountBadge.setText("0 ORDERS");
        } else {
            queueCountBadge.setText(activeCardMap.size() + (activeCardMap.size() == 1 ? " ORDER" : " ORDERS"));
            for (OrderCard card : activeCardMap.values()) {
                cardsContainer.add(card);
                cardsContainer.add(Box.createVerticalStrut(8));
            }
            cardsContainer.add(Box.createVerticalGlue());
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }
}
