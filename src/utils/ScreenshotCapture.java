package utils;

import core.OrderProcessingSystem;
import models.Order;
import ui.MainWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

/**
 * Automated Screenshot Runner:
 * Launches the simulator, generates realistic multi-threaded food orders,
 * lets workers process cards across lanes, captures high-resolution screenshots
 * of the UI in action, and saves them to the screenshots/ directory.
 */
public class ScreenshotCapture {

    public static void main(String[] args) throws Exception {
        File dir = new File("screenshots");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("Starting Automated Screenshot Capture...");

        OrderProcessingSystem system = new OrderProcessingSystem();
        final MainWindow[] windowHolder = new MainWindow[1];

        SwingUtilities.invokeAndWait(() -> {
            MainWindow window = new MainWindow(system);
            window.setSize(1360, 780);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            windowHolder[0] = window;
        });

        MainWindow window = windowHolder[0];

        // Seed 4 orders to fill all lanes (Kitchen, Packaging, Delivery)
        System.out.println("Enqueuing test orders...");
        system.addOrder(new Order(
                system.generateNextOrderId(),
                "Tony Stark",
                Arrays.asList("Pepperoni Pizza", "Cold Soda"),
                true // VIP
        ));

        Thread.sleep(400);
        system.addOrder(new Order(
                system.generateNextOrderId(),
                "Peter Parker",
                Arrays.asList("Double Cheeseburger", "Crispy Fries"),
                false
        ));

        Thread.sleep(400);
        system.addOrder(new Order(
                system.generateNextOrderId(),
                "Bruce Wayne",
                Arrays.asList("Dragon Sushi Roll", "Matcha Tea"),
                true // VIP
        ));

        Thread.sleep(400);
        system.addOrder(new Order(
                system.generateNextOrderId(),
                "Natasha Romanoff",
                Arrays.asList("Spicy Tacos (x3)", "Lemonade"),
                false
        ));

        // Wait for workers to distribute orders across Kitchen, Packaging, and Delivery lanes
        System.out.println("Waiting 3.8 seconds for orders to populate all 3 lanes...");
        Thread.sleep(3800);

        // Capture Screenshot 1: Live 3-Lane Processing Dashboard with Visible Buttons & Cards
        captureWindow(window, new File(dir, "foodflow_live_dashboard.png"));
        System.out.println("Captured: screenshots/foodflow_live_dashboard.png");

        // Wait for orders to complete so History Table has entries
        System.out.println("Waiting for orders to complete delivery...");
        Thread.sleep(8500);

        SwingUtilities.invokeAndWait(() -> {
            try {
                selectTab(window.getContentPane(), 0);
            } catch (Exception ignored) {
            }
        });
        Thread.sleep(500);

        // Capture Screenshot 2: Completed Orders & Telemetry Dashboard
        captureWindow(window, new File(dir, "foodflow_completed_table.png"));
        System.out.println("Captured: screenshots/foodflow_completed_table.png");

        // Switch tab to Thread Logs and capture Screenshot 3
        SwingUtilities.invokeAndWait(() -> {
            try {
                // Find tabbed pane and switch to tab 1 (Thread Logs)
                selectTab(window.getContentPane(), 1);
            } catch (Exception ignored) {
            }
        });

        Thread.sleep(800);
        captureWindow(window, new File(dir, "foodflow_thread_terminal.png"));
        System.out.println("Captured: screenshots/foodflow_thread_terminal.png");

        System.out.println("ALL SCREENSHOTS CAPTURED SUCCESSFULLY!");
        system.shutdown();
        window.dispose();
        System.exit(0);
    }

    private static void selectTab(Container container, int index) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTabbedPane) {
                JTabbedPane tp = (JTabbedPane) c;
                if (tp.getTabCount() > index) {
                    tp.setSelectedIndex(index);
                }
                return;
            } else if (c instanceof Container) {
                selectTab((Container) c, index);
            }
        }
    }

    private static void captureWindow(JFrame window, File targetFile) throws Exception {
        BufferedImage image = new BufferedImage(window.getWidth(), window.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        SwingUtilities.invokeAndWait(() -> window.paint(g2d));
        g2d.dispose();

        ImageIO.write(image, "PNG", targetFile);
    }
}
