package ui;

import core.SystemStatistics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Neobrutalist Statistics & Telemetry Dashboard.
 * Displays high-contrast stat boxes with bold typography and live numbers.
 */
public class StatsPanel extends JPanel {

    private JLabel totalCompletedLabel;
    private JLabel activeOrdersLabel;
    private JLabel avgTimeLabel;
    private JLabel kitchenQueueLabel;
    private JLabel packagingQueueLabel;
    private JLabel deliveryQueueLabel;

    public StatsPanel() {
        setLayout(new GridLayout(1, 6, 8, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 8, 0));

        buildStats();
    }

    private void buildStats() {
        totalCompletedLabel = createStatBox("COMPLETED", "0", NeobrutalismTheme.ACCENT_GREEN);
        activeOrdersLabel = createStatBox("IN-FLIGHT", "0", NeobrutalismTheme.ACCENT_YELLOW);
        avgTimeLabel = createStatBox("AVG TIME", "0.0s", NeobrutalismTheme.ACCENT_CYAN);
        kitchenQueueLabel = createStatBox("KITCHEN Q", "0", NeobrutalismTheme.ACCENT_BLUE);
        packagingQueueLabel = createStatBox("PACK Q", "0", NeobrutalismTheme.ACCENT_ORANGE);
        deliveryQueueLabel = createStatBox("DELIVERY Q", "0", NeobrutalismTheme.ACCENT_PURPLE);
    }

    private JLabel createStatBox(String title, String initialValue, Color topAccent) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBackground(NeobrutalismTheme.COLOR_WHITE);
        box.setBorder(new NeobrutalismTheme.HardShadowBorder(3, 3, 6));

        // Top accent line
        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(10, 4));
        accentBar.setBackground(topAccent);
        box.add(accentBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(NeobrutalismTheme.FONT_SMALL);
        titleLbl.setForeground(NeobrutalismTheme.COLOR_BLACK);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valLbl = new JLabel(initialValue);
        valLbl.setFont(NeobrutalismTheme.FONT_BIG_NUMBER);
        valLbl.setForeground(NeobrutalismTheme.COLOR_BLACK);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLbl);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(valLbl);

        box.add(contentPanel, BorderLayout.CENTER);
        add(box);

        return valLbl;
    }

    /**
     * Updates all statistics display from the core system statistics object.
     */
    public void updateStats(SystemStatistics stats) {
        if (stats == null) return;
        totalCompletedLabel.setText(String.valueOf(stats.getTotalCompleted()));
        activeOrdersLabel.setText(String.valueOf(stats.getActiveOrdersCount()));
        avgTimeLabel.setText(String.format("%.1fs", stats.getAverageProcessingTimeSeconds()));
        kitchenQueueLabel.setText(String.valueOf(stats.getKitchenQueueSize()));
        packagingQueueLabel.setText(String.valueOf(stats.getPackagingQueueSize()));
        deliveryQueueLabel.setText(String.valueOf(stats.getDeliveryQueueSize()));
    }
}
