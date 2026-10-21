package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread Status Monitor Component:
 * Inspects multi-threaded worker activity, thread states (RUNNING/IDLE/PAUSED),
 * individual thread throughput, and currently locked resources/orders.
 */
public class ThreadMonitorPanel extends JPanel {

    private final Map<String, WorkerCard> workerCards = new HashMap<>();

    public ThreadMonitorPanel() {
        setLayout(new BorderLayout(0, 6));
        setBackground(NeobrutalismTheme.COLOR_PANEL_BG);
        setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        buildHeader();
        buildWorkersGrid();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(NeobrutalismTheme.COLOR_BLACK);
        header.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel title = new JLabel("⚡ THREAD EXECUTION MONITOR");
        title.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        title.setForeground(NeobrutalismTheme.COLOR_WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel info = new JLabel("3 CONCURRENT CO-ROUTINES");
        info.setFont(NeobrutalismTheme.FONT_SMALL);
        info.setForeground(NeobrutalismTheme.ACCENT_YELLOW);
        header.add(info, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildWorkersGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 8, 0));
        grid.setBackground(NeobrutalismTheme.COLOR_BG);
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));

        WorkerCard kitchen = new WorkerCard("Chef-Marco (Kitchen-1)", "🍳", NeobrutalismTheme.ACCENT_BLUE);
        WorkerCard packaging = new WorkerCard("Packer-Alex (Packaging-1)", "📦", NeobrutalismTheme.ACCENT_YELLOW);
        WorkerCard delivery = new WorkerCard("Rider-Dash (Delivery-1)", "🚀", NeobrutalismTheme.ACCENT_GREEN);

        workerCards.put("Chef-Marco", kitchen);
        workerCards.put("KitchenWorker-1", kitchen);

        workerCards.put("Packer-Alex", packaging);
        workerCards.put("PackagingWorker-1", packaging);

        workerCards.put("Rider-Dash", delivery);
        workerCards.put("DeliveryWorker-1", delivery);

        grid.add(kitchen);
        grid.add(packaging);
        grid.add(delivery);

        add(grid, BorderLayout.CENTER);
    }

    public void updateWorkerState(String workerName, String threadState, int processedCount, String currentOrderId) {
        WorkerCard card = workerCards.get(workerName);
        if (card != null) {
            card.update(threadState, processedCount, currentOrderId);
        }
    }

    private static class WorkerCard extends JPanel {
        private final JLabel stateBadge;
        private final JLabel countLabel;
        private final JLabel orderLabel;

        public WorkerCard(String name, String icon, Color accent) {
            setLayout(new BorderLayout(0, 4));
            setBackground(NeobrutalismTheme.COLOR_WHITE);
            setBorder(new NeobrutalismTheme.HardShadowBorder(2, 2, 6));

            // Header strip
            JPanel strip = new JPanel(new BorderLayout(4, 0));
            strip.setBackground(accent);
            strip.setBorder(new EmptyBorder(4, 6, 4, 6));

            JLabel title = new JLabel(icon + " " + name);
            title.setFont(NeobrutalismTheme.FONT_SMALL);
            title.setForeground(NeobrutalismTheme.COLOR_BLACK);
            strip.add(title, BorderLayout.WEST);

            add(strip, BorderLayout.NORTH);

            // Body
            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);
            body.setBorder(new EmptyBorder(4, 6, 4, 6));

            JPanel stateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            stateRow.setOpaque(false);

            stateBadge = new JLabel("IDLE");
            stateBadge.setFont(NeobrutalismTheme.FONT_SMALL);
            stateBadge.setOpaque(true);
            stateBadge.setBackground(NeobrutalismTheme.COLOR_MUTED);
            stateBadge.setForeground(NeobrutalismTheme.COLOR_BLACK);
            stateBadge.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(1, 5, 1, 5)
            ));
            stateRow.add(stateBadge);

            countLabel = new JLabel("Cooked / Handled: 0");
            countLabel.setFont(NeobrutalismTheme.FONT_SMALL);
            countLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);

            orderLabel = new JLabel("Active Order: None");
            orderLabel.setFont(NeobrutalismTheme.FONT_SMALL);
            orderLabel.setForeground(new Color(0x55, 0x55, 0x55));

            body.add(stateRow);
            body.add(Box.createVerticalStrut(4));
            body.add(countLabel);
            body.add(Box.createVerticalStrut(2));
            body.add(orderLabel);

            add(body, BorderLayout.CENTER);
        }

        public void update(String state, int count, String currentOrderId) {
            stateBadge.setText(state);
            countLabel.setText("Processed Orders: " + count);
            orderLabel.setText("Active Order: " + (currentOrderId != null ? currentOrderId : "None"));

            if ("PAUSED".equalsIgnoreCase(state)) {
                stateBadge.setBackground(NeobrutalismTheme.ACCENT_RED);
                stateBadge.setForeground(NeobrutalismTheme.COLOR_WHITE);
            } else if ("IDLE".equalsIgnoreCase(state)) {
                stateBadge.setBackground(NeobrutalismTheme.COLOR_MUTED);
                stateBadge.setForeground(NeobrutalismTheme.COLOR_BLACK);
            } else {
                stateBadge.setBackground(NeobrutalismTheme.ACCENT_GREEN);
                stateBadge.setForeground(NeobrutalismTheme.COLOR_BLACK);
            }
            repaint();
        }
    }
}
