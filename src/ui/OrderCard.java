package ui;

import models.Order;
import models.OrderStatus;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Custom Neobrutalist Order Card Component.
 * Visualizes an individual order's progress, items, customer, and real-time stage progress bar.
 */
public class OrderCard extends JPanel {

    private final Order order;
    private final Consumer<String> onCancelRequested;

    private JLabel idLabel;
    private JLabel customerLabel;
    private JLabel itemsLabel;
    private JLabel statusPill;
    private JLabel timerLabel;
    private JLabel workerLabel;
    private JProgressBar progressBar;

    public OrderCard(Order order, Consumer<String> onCancelRequested) {
        this.order = order;
        this.onCancelRequested = onCancelRequested;

        setLayout(new BorderLayout(4, 4));
        setBackground(NeobrutalismTheme.COLOR_WHITE);
        setBorder(new NeobrutalismTheme.HardShadowBorder(3, 3, 6));
        setPreferredSize(new Dimension(300, 160));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        buildUI();
        updateDisplay();
    }

    private void buildUI() {
        // --- TOP ROW: Order ID & VIP Tag & Cancel Button ---
        JPanel topPanel = new JPanel(new BorderLayout(4, 0));
        topPanel.setOpaque(false);

        JPanel leftBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftBadges.setOpaque(false);

        idLabel = new JLabel(order.getOrderId());
        idLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        idLabel.setOpaque(true);
        idLabel.setBackground(NeobrutalismTheme.COLOR_BLACK);
        idLabel.setForeground(NeobrutalismTheme.COLOR_WHITE);
        idLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        leftBadges.add(idLabel);

        if (order.isVip()) {
            JLabel vipBadge = new JLabel("⭐ VIP");
            vipBadge.setFont(NeobrutalismTheme.FONT_SMALL);
            vipBadge.setOpaque(true);
            vipBadge.setBackground(NeobrutalismTheme.ACCENT_PURPLE);
            vipBadge.setForeground(NeobrutalismTheme.COLOR_WHITE);
            vipBadge.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(1, 5, 1, 5)
            ));
            leftBadges.add(vipBadge);
        }

        topPanel.add(leftBadges, BorderLayout.WEST);

        // Cancel button (only active if not already delivered)
        if (!order.getStatus().isTerminal()) {
            JButton cancelBtn = new JButton("✕");
            cancelBtn.setFont(new Font("Courier New", Font.BOLD, 12));
            cancelBtn.setBackground(NeobrutalismTheme.ACCENT_RED);
            cancelBtn.setForeground(NeobrutalismTheme.COLOR_WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false));
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.setToolTipText("Cancel Order");
            cancelBtn.setPreferredSize(new Dimension(24, 22));
            cancelBtn.addActionListener(e -> {
                if (onCancelRequested != null) {
                    onCancelRequested.accept(order.getOrderId());
                }
            });
            topPanel.add(cancelBtn, BorderLayout.EAST);
        }

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Customer Name, Items, Worker ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 2, 4, 2));

        customerLabel = new JLabel("👤 " + order.getCustomerName());
        customerLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        customerLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        itemsLabel = new JLabel("🍽 " + truncate(order.getItemsAsString(), 36));
        itemsLabel.setFont(NeobrutalismTheme.FONT_SMALL);
        itemsLabel.setForeground(new Color(0x33, 0x33, 0x33));
        itemsLabel.setToolTipText(order.getItemsAsString());
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        workerLabel = new JLabel("⚙ " + order.getCurrentWorkerName());
        workerLabel.setFont(NeobrutalismTheme.FONT_SMALL);
        workerLabel.setForeground(new Color(0x55, 0x55, 0x55));
        workerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(customerLabel);
        centerPanel.add(Box.createVerticalStrut(2));
        centerPanel.add(itemsLabel);
        centerPanel.add(Box.createVerticalStrut(2));
        centerPanel.add(workerLabel);

        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM: Status Pill & Elapsed Time & Progress Bar ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        JPanel statusRow = new JPanel(new BorderLayout(4, 0));
        statusRow.setOpaque(false);

        statusPill = new JLabel(order.getStatus().getDisplayName());
        statusPill.setFont(NeobrutalismTheme.FONT_SMALL);
        statusPill.setOpaque(true);
        statusPill.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(2, 6, 2, 6)
        ));
        statusRow.add(statusPill, BorderLayout.WEST);

        timerLabel = new JLabel("0.0s");
        timerLabel.setFont(NeobrutalismTheme.FONT_SMALL);
        timerLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        statusRow.add(timerLabel, BorderLayout.EAST);

        bottomPanel.add(statusRow);
        bottomPanel.add(Box.createVerticalStrut(4));

        // Linear Progress Bar (Custom styled sharp neobrutal progress bar)
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(280, 10));
        progressBar.setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false));
        progressBar.setBackground(NeobrutalismTheme.COLOR_WHITE);
        progressBar.setForeground(NeobrutalismTheme.COLOR_BLACK);
        bottomPanel.add(progressBar);

        add(bottomPanel, BorderLayout.SOUTH);

        // Hover effect for card
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(0xFD, 0xFD, 0xF9));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(NeobrutalismTheme.COLOR_WHITE);
                repaint();
            }
        });
    }

    /**
     * Refreshes the real-time timer, progress bar, and status pill colors.
     */
    public void updateDisplay() {
        OrderStatus status = order.getStatus();
        statusPill.setText(status.getDisplayName());
        workerLabel.setText("⚙ " + order.getCurrentWorkerName());

        // Color coding per requirement
        Color pillBg = Color.decode(status.getHexColor());
        statusPill.setBackground(pillBg);
        if (status == OrderStatus.PREPARING) {
            statusPill.setForeground(NeobrutalismTheme.COLOR_WHITE);
            progressBar.setForeground(NeobrutalismTheme.ACCENT_BLUE);
        } else if (status == OrderStatus.PACKAGING) {
            statusPill.setForeground(NeobrutalismTheme.COLOR_BLACK);
            progressBar.setForeground(NeobrutalismTheme.ACCENT_YELLOW);
        } else if (status == OrderStatus.DELIVERING || status == OrderStatus.DELIVERED) {
            statusPill.setForeground(NeobrutalismTheme.COLOR_BLACK);
            progressBar.setForeground(NeobrutalismTheme.ACCENT_GREEN);
        } else {
            statusPill.setForeground(NeobrutalismTheme.COLOR_BLACK);
            progressBar.setForeground(NeobrutalismTheme.COLOR_BLACK);
        }

        // Timer
        double elapsedSec = order.getStageElapsedMs() / 1000.0;
        double totalSec = order.getTotalProcessingTimeSeconds();
        timerLabel.setText(String.format("Stage: %.1fs (Tot: %.1fs)", elapsedSec, totalSec));

        // Progress bar value
        int progressPercent = (int) (order.getStageProgress() * 100.0);
        progressBar.setValue(progressPercent);
    }

    public Order getOrder() {
        return order;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
