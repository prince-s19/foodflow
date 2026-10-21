package ui;

import core.OrderProcessingSystem;
import models.Order;
import utils.SoundManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Order Input Section & Simulation Control Deck.
 * Built with strict Neobrutalism UI style:
 * - Thick solid borders
 * - Chunky action buttons with physical press response
 * - Food quick-select item chips
 * - Form validation with red border highlighting
 */
public class OrderInputPanel extends JPanel {

    private final OrderProcessingSystem system;
    private final Random random = new Random();

    private JTextField customerField;
    private JTextField itemsField;
    private JLabel orderIdPreview;
    private JCheckBox vipCheckBox;
    private JLabel errorBanner;
    private JButton pauseResumeBtn;
    private JButton soundToggleBtn;

    // Preset customer names for random order generation
    private static final String[] RANDOM_NAMES = {
            "Alice Smith", "Bob Vance", "Charlie Day", "Diana Prince", "Ethan Hunt",
            "Fiona Gallagher", "George Clark", "Hannah Montana", "Ian Malcolm", "Julia Roberts"
    };

    // Preset food items
    private static final String[][] FOOD_COMBOS = {
            {"🍕 Pepperoni Pizza", "🥤 Cold Soda"},
            {"🍔 Double Cheeseburger", "🍟 Crispy Fries"},
            {"🌮 Spicy Tacos (x3)", "🥑 Guacamole Bowl"},
            {"🍣 Salmon Sushi Roll", "🍵 Green Tea"},
            {"🥪 Club Sandwich", "🍟 Loaded Fries", "🥤 Lemonade"},
            {"🥗 Caesar Salad", "🥖 Garlic Breadsticks"}
    };

    public OrderInputPanel(OrderProcessingSystem system) {
        this.system = system;

        setLayout(new BorderLayout(0, 8));
        setBackground(NeobrutalismTheme.COLOR_PANEL_BG);
        setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        buildHeader();
        buildForm();
        buildActionButtons();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NeobrutalismTheme.ACCENT_YELLOW);
        header.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("📝 DISPATCH NEW ORDER");
        title.setFont(NeobrutalismTheme.FONT_HEADER);
        title.setForeground(NeobrutalismTheme.COLOR_BLACK);
        header.add(title, BorderLayout.WEST);

        orderIdPreview = new JLabel("NEXT ID: " + peekNextId());
        orderIdPreview.setFont(NeobrutalismTheme.FONT_SMALL);
        orderIdPreview.setOpaque(true);
        orderIdPreview.setBackground(NeobrutalismTheme.COLOR_BLACK);
        orderIdPreview.setForeground(NeobrutalismTheme.COLOR_WHITE);
        orderIdPreview.setBorder(new EmptyBorder(3, 8, 3, 8));
        header.add(orderIdPreview, BorderLayout.EAST);

        // Divider
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(100, 3));
        sep.setBackground(NeobrutalismTheme.COLOR_BLACK);

        JPanel combined = new JPanel(new BorderLayout());
        combined.add(header, BorderLayout.CENTER);
        combined.add(sep, BorderLayout.SOUTH);

        add(combined, BorderLayout.NORTH);
    }

    private void buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(NeobrutalismTheme.COLOR_WHITE);
        form.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Error Banner
        errorBanner = new JLabel(" ");
        errorBanner.setFont(NeobrutalismTheme.FONT_SMALL);
        errorBanner.setForeground(NeobrutalismTheme.ACCENT_RED);
        errorBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorBanner);
        form.add(Box.createVerticalStrut(4));

        // Customer Name Input
        JLabel customerLabel = new JLabel("CUSTOMER NAME *");
        customerLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        customerLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(customerLabel);
        form.add(Box.createVerticalStrut(2));

        customerField = new JTextField("Sarah Connor");
        customerField.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        customerField.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(6, 8, 6, 8)
        ));
        customerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        customerField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(customerField);
        form.add(Box.createVerticalStrut(8));

        // Items Input
        JLabel itemsLabel = new JLabel("FOOD ITEMS (comma-separated) *");
        itemsLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        itemsLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(itemsLabel);
        form.add(Box.createVerticalStrut(2));

        itemsField = new JTextField("🍕 Pepperoni Pizza, 🥤 Cold Soda");
        itemsField.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        itemsField.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(6, 8, 6, 8)
        ));
        itemsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        itemsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(itemsField);
        form.add(Box.createVerticalStrut(8));

        // Quick Add Chips
        JLabel chipsLabel = new JLabel("QUICK ADD MENU PRESETS:");
        chipsLabel.setFont(NeobrutalismTheme.FONT_SMALL);
        chipsLabel.setForeground(new Color(0x55, 0x55, 0x55));
        chipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chipsLabel);
        form.add(Box.createVerticalStrut(4));

        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        chipsPanel.setOpaque(false);
        chipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] quickChips = {"🍕 Pizza", "🍔 Burger", "🍟 Fries", "🥤 Soda", "🌮 Tacos", "🍣 Sushi", "🍩 Donut"};
        for (String chip : quickChips) {
            JButton chipBtn = new JButton(chip);
            chipBtn.setFont(NeobrutalismTheme.FONT_SMALL);
            chipBtn.setBackground(NeobrutalismTheme.COLOR_MUTED);
            chipBtn.setForeground(NeobrutalismTheme.COLOR_BLACK);
            chipBtn.setFocusPainted(false);
            chipBtn.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(2, 6, 2, 6)
            ));
            chipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chipBtn.addActionListener(e -> {
                String current = itemsField.getText().trim();
                if (current.isEmpty()) {
                    itemsField.setText(chip);
                } else {
                    itemsField.setText(current + ", " + chip);
                }
            });
            chipsPanel.add(chipBtn);
        }
        form.add(chipsPanel);
        form.add(Box.createVerticalStrut(8));

        // Priority / VIP Checkbox
        vipCheckBox = new JCheckBox("⭐ VIP Priority Order (Preempts queue & cooks faster)");
        vipCheckBox.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        vipCheckBox.setForeground(NeobrutalismTheme.ACCENT_PURPLE);
        vipCheckBox.setOpaque(false);
        vipCheckBox.setFocusPainted(false);
        vipCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(vipCheckBox);

        add(form, BorderLayout.CENTER);
    }

    private void buildActionButtons() {
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setBackground(NeobrutalismTheme.COLOR_BG);
        btnPanel.setBorder(new EmptyBorder(8, 12, 12, 12));

        // Row 1: Submit Order Button (Huge primary action)
        JButton submitBtn = NeobrutalismTheme.createButton(
                "🚀 DISPATCH ORDER TO KITCHEN",
                NeobrutalismTheme.ACCENT_GREEN,
                NeobrutalismTheme.COLOR_BLACK
        );
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> submitOrder());
        btnPanel.add(submitBtn);
        btnPanel.add(Box.createVerticalStrut(8));

        // Row 2: Batch Simulation and Presets
        JPanel subRow = new JPanel(new GridLayout(1, 2, 6, 0));
        subRow.setOpaque(false);
        subRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton batchBtn = NeobrutalismTheme.createButton(
                "⚡ BATCH (5 ORDERS)",
                NeobrutalismTheme.ACCENT_CYAN,
                NeobrutalismTheme.COLOR_BLACK
        );
        batchBtn.addActionListener(e -> simulateRandomBatch(5));
        subRow.add(batchBtn);

        JButton singleRandomBtn = NeobrutalismTheme.createButton(
                "🎲 RANDOM ORDER",
                NeobrutalismTheme.ACCENT_YELLOW,
                NeobrutalismTheme.COLOR_BLACK
        );
        singleRandomBtn.addActionListener(e -> simulateRandomOrder());
        subRow.add(singleRandomBtn);

        btnPanel.add(subRow);
        btnPanel.add(Box.createVerticalStrut(8));

        // Row 3: Pause/Resume & Audio Toggle & Clear Inputs
        JPanel controlRow = new JPanel(new GridLayout(1, 2, 6, 0));
        controlRow.setOpaque(false);
        controlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        pauseResumeBtn = NeobrutalismTheme.createButton(
                "⏸ PAUSE SYSTEM",
                NeobrutalismTheme.COLOR_WHITE,
                NeobrutalismTheme.COLOR_BLACK
        );
        pauseResumeBtn.addActionListener(e -> togglePause());
        controlRow.add(pauseResumeBtn);

        soundToggleBtn = NeobrutalismTheme.createButton(
                "🔊 SOUND: ON",
                NeobrutalismTheme.COLOR_WHITE,
                NeobrutalismTheme.COLOR_BLACK
        );
        soundToggleBtn.addActionListener(e -> toggleSound());
        controlRow.add(soundToggleBtn);

        btnPanel.add(controlRow);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void submitOrder() {
        String customer = customerField.getText().trim();
        String itemsRaw = itemsField.getText().trim();

        // Validation
        boolean valid = true;
        if (customer.isEmpty()) {
            customerField.setBorder(new LineBorder(NeobrutalismTheme.ACCENT_RED, 3, false));
            valid = false;
        } else {
            customerField.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }

        if (itemsRaw.isEmpty()) {
            itemsField.setBorder(new LineBorder(NeobrutalismTheme.ACCENT_RED, 3, false));
            valid = false;
        } else {
            itemsField.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }

        if (!valid) {
            errorBanner.setText("⚠ Please fill in both Customer Name and Food Items!");
            return;
        }

        errorBanner.setText(" ");
        String[] parts = itemsRaw.split(",");
        List<String> items = new ArrayList<>();
        for (String p : parts) {
            if (!p.trim().isEmpty()) {
                items.add(p.trim());
            }
        }

        String orderId = system.generateNextOrderId();
        boolean isVip = vipCheckBox.isSelected();
        Order order = new Order(orderId, customer, items, isVip);

        system.addOrder(order);

        // Reset inputs
        vipCheckBox.setSelected(false);
        customerField.setText(RANDOM_NAMES[random.nextInt(RANDOM_NAMES.length)]);
        String[] nextCombo = FOOD_COMBOS[random.nextInt(FOOD_COMBOS.length)];
        itemsField.setText(String.join(", ", nextCombo));
        orderIdPreview.setText("NEXT ID: " + peekNextId());
    }

    public void simulateRandomOrder() {
        String customer = RANDOM_NAMES[random.nextInt(RANDOM_NAMES.length)];
        String[] combo = FOOD_COMBOS[random.nextInt(FOOD_COMBOS.length)];
        boolean isVip = random.nextBoolean() && random.nextBoolean(); // ~25% VIP
        String orderId = system.generateNextOrderId();
        Order order = new Order(orderId, customer, Arrays.asList(combo), isVip);
        system.addOrder(order);
        orderIdPreview.setText("NEXT ID: " + peekNextId());
    }

    public void simulateRandomBatch(int count) {
        new Thread(() -> {
            for (int i = 0; i < count; i++) {
                simulateRandomOrder();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }
            }
        }, "Batch-Simulator").start();
    }

    private void togglePause() {
        system.togglePause();
        if (system.isPaused()) {
            pauseResumeBtn.setText("▶ RESUME SYSTEM");
            pauseResumeBtn.setBackground(NeobrutalismTheme.ACCENT_RED);
            pauseResumeBtn.setForeground(NeobrutalismTheme.COLOR_WHITE);
        } else {
            pauseResumeBtn.setText("⏸ PAUSE SYSTEM");
            pauseResumeBtn.setBackground(NeobrutalismTheme.COLOR_WHITE);
            pauseResumeBtn.setForeground(NeobrutalismTheme.COLOR_BLACK);
        }
    }

    private void toggleSound() {
        SoundManager.toggleSound();
        if (SoundManager.isSoundEnabled()) {
            soundToggleBtn.setText("🔊 SOUND: ON");
        } else {
            soundToggleBtn.setText("🔇 SOUND: MUTED");
        }
    }

    private String peekNextId() {
        return "ORD-" + (system.getStatistics().getTotalSubmitted() + 1001);
    }
}
