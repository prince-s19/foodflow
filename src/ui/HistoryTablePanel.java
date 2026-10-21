package ui;

import core.OrderProcessingSystem;
import models.Order;
import utils.Logger;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Order History & Audit Log Section.
 * Implements a high-contrast Neobrutalist table displaying completed orders,
 * timing breakdowns, search filtering, and CSV export capabilities.
 */
public class HistoryTablePanel extends JPanel {

    private final OrderProcessingSystem system;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JLabel countLabel;

    private static final String[] COLUMNS = {
            "ORDER ID", "CUSTOMER", "ITEMS", "PRIORITY", "TOTAL TIME", "KITCHEN", "PACKAGING", "DELIVERY", "COMPLETED AT"
    };

    public HistoryTablePanel(OrderProcessingSystem system) {
        this.system = system;

        setLayout(new BorderLayout(0, 8));
        setBackground(NeobrutalismTheme.COLOR_PANEL_BG);
        setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        buildHeader();
        buildTableArea();
        buildFooter();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(NeobrutalismTheme.ACCENT_GREEN);
        header.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("📜 COMPLETED ORDERS & AUDIT LOG");
        title.setFont(NeobrutalismTheme.FONT_HEADER);
        title.setForeground(NeobrutalismTheme.COLOR_BLACK);
        header.add(title, BorderLayout.WEST);

        // Search Bar in Header
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        searchPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍 FILTER:");
        searchIcon.setFont(NeobrutalismTheme.FONT_SMALL);
        searchIcon.setForeground(NeobrutalismTheme.COLOR_BLACK);
        searchPanel.add(searchIcon);

        searchField = new JTextField(12);
        searchField.setFont(NeobrutalismTheme.FONT_SMALL);
        searchField.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(2, 6, 2, 6)
        ));
        searchField.addCaretListener(e -> filterTable());
        searchPanel.add(searchField);

        header.add(searchPanel, BorderLayout.EAST);

        // Divider
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(100, 3));
        sep.setBackground(NeobrutalismTheme.COLOR_BLACK);

        JPanel combined = new JPanel(new BorderLayout());
        combined.add(header, BorderLayout.CENTER);
        combined.add(sep, BorderLayout.SOUTH);

        add(combined, BorderLayout.NORTH);
    }

    private void buildTableArea() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(NeobrutalismTheme.FONT_BODY);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(NeobrutalismTheme.COLOR_BLACK);
        table.setSelectionBackground(NeobrutalismTheme.ACCENT_YELLOW);
        table.setSelectionForeground(NeobrutalismTheme.COLOR_BLACK);

        // Header Styling
        JTableHeader th = table.getTableHeader();
        th.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        th.setBackground(NeobrutalismTheme.COLOR_BLACK);
        th.setForeground(NeobrutalismTheme.COLOR_WHITE);
        th.setPreferredSize(new Dimension(100, 32));
        th.setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 1, false));

        // Center / Left Alignment renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(new EmptyBorder(0, 6, 0, 6));

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Order ID
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);   // Customer
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);   // Items
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Priority
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Total Time
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Kitchen
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Packaging
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Delivery
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // Completed At

        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false));
        scrollPane.getViewport().setBackground(NeobrutalismTheme.COLOR_WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBackground(NeobrutalismTheme.COLOR_BG);
        footer.setBorder(new EmptyBorder(6, 12, 6, 12));

        countLabel = new JLabel("TOTAL LOGGED: 0 ORDERS");
        countLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        countLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        footer.add(countLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton exportCsvBtn = NeobrutalismTheme.createButton(
                "📥 EXPORT CSV",
                NeobrutalismTheme.ACCENT_CYAN,
                NeobrutalismTheme.COLOR_BLACK
        );
        exportCsvBtn.setPreferredSize(new Dimension(140, 32));
        exportCsvBtn.addActionListener(e -> exportCsv());
        btnPanel.add(exportCsvBtn);

        JButton clearBtn = NeobrutalismTheme.createButton(
                "🗑 CLEAR HISTORY",
                NeobrutalismTheme.ACCENT_RED,
                NeobrutalismTheme.COLOR_WHITE
        );
        clearBtn.setPreferredSize(new Dimension(140, 32));
        clearBtn.addActionListener(e -> clearHistory());
        btnPanel.add(clearBtn);

        footer.add(btnPanel, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    public synchronized void reloadOrders() {
        filterTable();
    }

    private synchronized void filterTable() {
        String filter = searchField.getText().trim().toLowerCase();
        List<Order> orders = system.getCompletedOrders();

        tableModel.setRowCount(0);
        int matched = 0;

        for (Order o : orders) {
            boolean match = filter.isEmpty() ||
                    o.getOrderId().toLowerCase().contains(filter) ||
                    o.getCustomerName().toLowerCase().contains(filter) ||
                    o.getItemsAsString().toLowerCase().contains(filter);

            if (match) {
                tableModel.addRow(new Object[]{
                        o.getOrderId(),
                        o.getCustomerName(),
                        o.getItemsAsString(),
                        o.isVip() ? "⭐ VIP" : "STANDARD",
                        String.format("%.2fs", o.getTotalProcessingTimeSeconds()),
                        String.format("%.1fs", o.getKitchenDurationMs() / 1000.0),
                        String.format("%.1fs", o.getPackagingDurationMs() / 1000.0),
                        String.format("%.1fs", o.getDeliveryDurationMs() / 1000.0),
                        o.getFormattedCompletedTime()
                });
                matched++;
            }
        }

        countLabel.setText(String.format("TOTAL LOGGED: %d ORDERS%s",
                orders.size(),
                filter.isEmpty() ? "" : " (" + matched + " matching)"));
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear completed order history?",
                "CONFIRM CLEAR",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            system.clearHistory();
            reloadOrders();
        }
    }

    private void exportCsv() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("foodflow_orders_" + System.currentTimeMillis() + ".csv"));
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(system.exportHistoryToCsv());
                }
                Logger.success("Exported order history to CSV: " + file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Order history exported successfully to:\n" + file.getName(),
                        "EXPORT SUCCESS", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            Logger.error("Failed to export CSV: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(),
                    "EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }
}
