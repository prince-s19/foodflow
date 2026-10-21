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
 * - Thick solid black borders
 * - Chunky action buttons with guaranteed visible text
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

    private static final String[] RANDOM_NAMES = {
            "Sarah Connor", "Tony Stark", "Peter Parker", "Bruce Wayne", "Clark Kent",
            "Diana Prince", "Walter White", "Luke Skywalker", "Natasha Romanoff", "John Wick"
    };

    private static final String[][] FOOD_COMBOS = {
            {"Pepperoni Pizza", "Cold Soda"},
            {"Double Cheeseburger", "Crispy Fries"},
            {"Spicy Tacos (x3)", "Guacamole Bowl"},
            {"Salmon Sushi Roll", "Green Tea"},
            {"Club Sandwich", "Loaded Fries", "Lemonade"},
            {"Caesar Salad", "Garlic Breadsticks"}
    };

    public OrderInputPanel(OrderProcessingSystem system) {
        this.system = system;

        setLayout(new BorderLayout(0, 0));
        setBackground(NeobrutalismTheme.COLOR_PANEL_BG);
        setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        buildHeader();
        buildBody();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NeobrutalismTheme.ACCENT_YELLOW);
        header.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("[DISPATCH] NEW FOOD ORDER");
        title.setFont(NeobrutalismTheme.FONT_HEADER);
        title.setForeground(NeobrutalismTheme.COLOR_BLACK);
        header.add(title, BorderLayout.WEST);

        orderIdPreview = new JLabel("NEXT: " + peekNextId());
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

    private void buildBody() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(NeobrutalismTheme.COLOR_WHITE);
        container.setBorder(new EmptyBorder(10, 12, 12, 12));

        // Error Banner
        errorBanner = new JLabel(" ");
        errorBanner.setFont(NeobrutalismTheme.FONT_SMALL);
        errorBanner.setForeground(NeobrutalismTheme.ACCENT_RED);
        errorBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(errorBanner);
        container.add(Box.createVerticalStrut(2));

        // Customer Name Input
        JLabel customerLabel = new JLabel("CUSTOMER NAME *");
        customerLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        customerLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(customerLabel);
        container.add(Box.createVerticalStrut(2));

        customerField = new JTextField("Sarah Connor");
        customerField.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        customerField.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(5, 8, 5, 8)
        ));
        customerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        customerField.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(customerField);
        container.add(Box.createVerticalStrut(6));

        // Items Input
        JLabel itemsLabel = new JLabel("FOOD ITEMS (comma-separated) *");
        itemsLabel.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        itemsLabel.setForeground(NeobrutalismTheme.COLOR_BLACK);
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(itemsLabel);
        container.add(Box.createVerticalStrut(2));

        itemsField = new JTextField("Pepperoni Pizza, Cold Soda");
        itemsField.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        itemsField.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                new EmptyBorder(5, 8, 5, 8)
        ));
        itemsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        itemsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(itemsField);
        container.add(Box.createVerticalStrut(6));

        // Quick Add Chips
        JLabel chipsLabel = new JLabel("QUICK ADD MENU ITEMS:");
        chipsLabel.setFont(NeobrutalismTheme.FONT_SMALL);
        chipsLabel.setForeground(new Color(0x55, 0x55, 0x55));
        chipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(chipsLabel);
        container.add(Box.createVerticalStrut(2));

        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        chipsPanel.setOpaque(false);
        chipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] quickChips = {"Pizza", "Burger", "Fries", "Soda", "Tacos", "Sushi", "Donut"};
        for (String chip : quickChips) {
            JButton chipBtn = new JButton("+ " + chip);
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
        container.add(chipsPanel);
        container.add(Box.createVerticalStrut(6));

        // Priority / VIP Checkbox
        vipCheckBox = new JCheckBox("★ VIP Priority Order (Preempts queue & expedited prep)");
        vipCheckBox.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        vipCheckBox.setForeground(NeobrutalismTheme.ACCENT_PURPLE);
        vipCheckBox.setOpaque(false);
        vipCheckBox.setFocusPainted(false);
        vipCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(vipCheckBox);
        container.add(Box.createVerticalStrut(10));

        // Divider
        JPanel btnDivider = new JPanel();
        btnDivider.setPreferredSize(new Dimension(100, 2));
        btnDivider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        btnDivider.setBackground(NeobrutalismTheme.COLOR_BLACK);
        btnDivider.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(btnDivider);
        container.add(Box.createVerticalStrut(10));

        // --- BUTTONS SECTION (WITH GUARANTEED VISIBLE TEXT) ---

        // 1. Primary Dispatch Button (Large & prominent)
        JButton submitBtn = NeobrutalismTheme.createButton(
                "+ DISPATCH TO KITCHEN",
                NeobrutalismTheme.ACCENT_GREEN,
                NeobrutalismTheme.COLOR_BLACK
        );
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.setPreferredSize(new Dimension(400, 44));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> submitOrder());
        container.add(submitBtn);
        container.add(Box.createVerticalStrut(6));

        // 2. Batch & Random Grid
        JPanel subRow = new JPanel(new GridLayout(1, 2, 8, 0));
        subRow.setOpaque(false);
        subRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        subRow.setPreferredSize(new Dimension(400, 38));
        subRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton batchBtn = NeobrutalismTheme.createButton(
                "[5x] SIMULATE BATCH",
                NeobrutalismTheme.ACCENT_CYAN,
                NeobrutalismTheme.COLOR_BLACK
        );
        batchBtn.addActionListener(e -> simulateRandomBatch(5));
        subRow.add(batchBtn);

        JButton singleRandomBtn = NeobrutalismTheme.createButton(
                "[+] RANDOM ORDER",
                NeobrutalismTheme.ACCENT_YELLOW,
                NeobrutalismTheme.COLOR_BLACK
        );
        singleRandomBtn.addActionListener(e -> simulateRandomOrder());
        subRow.add(singleRandomBtn);

        container.add(subRow);
        container.add(Box.createVerticalStrut(6));

        // 3. Pause & Sound Control Grid
        JPanel controlRow = new JPanel(new GridLayout(1, 2, 8, 0));
        controlRow.setOpaque(false);
        controlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        controlRow.setPreferredSize(new Dimension(400, 36));
        controlRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        pauseResumeBtn = NeobrutalismTheme.createButton(
                "|| PAUSE SYSTEM",
                NeobrutalismTheme.COLOR_WHITE,
                NeobrutalismTheme.COLOR_BLACK
        );
        pauseResumeBtn.addActionListener(e -> togglePause());
        controlRow.add(pauseResumeBtn);

        soundToggleBtn = NeobrutalismTheme.createButton(
                "[VOL] SOUND: ON",
                NeobrutalismTheme.COLOR_WHITE,
                NeobrutalismTheme.COLOR_BLACK
        );
        soundToggleBtn.addActionListener(e -> toggleSound());
        controlRow.add(soundToggleBtn);

        container.add(controlRow);

        add(container, BorderLayout.CENTER);
    }

    private void submitOrder() {
        String customer = customerField.getText().trim();
        String itemsRaw = itemsField.getText().trim();

        boolean valid = true;
        if (customer.isEmpty()) {
            customerField.setBorder(new LineBorder(NeobrutalismTheme.ACCENT_RED, 3, false));
            valid = false;
        } else {
            customerField.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(5, 8, 5, 8)
            ));
        }

        if (itemsRaw.isEmpty()) {
            itemsField.setBorder(new LineBorder(NeobrutalismTheme.ACCENT_RED, 3, false));
            valid = false;
        } else {
            itemsField.setBorder(new CompoundBorder(
                    new LineBorder(NeobrutalismTheme.COLOR_BLACK, 2, false),
                    new EmptyBorder(5, 8, 5, 8)
            ));
        }

        if (!valid) {
            errorBanner.setText("! Please enter both Customer Name and Food Items");
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

        // Reset
        vipCheckBox.setSelected(false);
        customerField.setText(RANDOM_NAMES[random.nextInt(RANDOM_NAMES.length)]);
        String[] nextCombo = FOOD_COMBOS[random.nextInt(FOOD_COMBOS.length)];
        itemsField.setText(String.join(", ", nextCombo));
        orderIdPreview.setText("NEXT: " + peekNextId());
    }

    public void simulateRandomOrder() {
        String customer = RANDOM_NAMES[random.nextInt(RANDOM_NAMES.length)];
        String[] combo = FOOD_COMBOS[random.nextInt(FOOD_COMBOS.length)];
        boolean isVip = random.nextBoolean() && random.nextBoolean(); // ~25% VIP
        String orderId = system.generateNextOrderId();
        Order order = new Order(orderId, customer, Arrays.asList(combo), isVip);
        system.addOrder(order);
        orderIdPreview.setText("NEXT: " + peekNextId());
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
            pauseResumeBtn.setText("> RESUME SYSTEM");
        } else {
            pauseResumeBtn.setText("|| PAUSE SYSTEM");
        }
    }

    private void toggleSound() {
        SoundManager.toggleSound();
        if (SoundManager.isSoundEnabled()) {
            soundToggleBtn.setText("[VOL] SOUND: ON");
        } else {
            soundToggleBtn.setText("[MUTE] SOUND: OFF");
        }
    }

    private String peekNextId() {
        return "ORD-" + (system.getStatistics().getTotalSubmitted() + 1001);
    }
}
