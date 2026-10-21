import core.OrderProcessingSystem;
import models.Order;
import ui.MainWindow;
import utils.Logger;

import javax.swing.*;
import java.util.Arrays;

/**
 * Entry point for FoodFlow - Online Food Order Processing Simulator.
 * Boots the multi-threaded backend orchestrator and launches the Neobrutalist UI.
 */
public class Main {

    public static void main(String[] args) {
        printAsciiBanner();

        // Initialize backend threading engine
        OrderProcessingSystem system = new OrderProcessingSystem();

        // Launch UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow window = new MainWindow(system);
                window.setVisible(true);

                Logger.system("GUI window opened successfully.");

                // Seed initial demo orders so user immediately experiences concurrent thread execution
                seedDemoOrders(system);

            } catch (Exception e) {
                Logger.error("Failed to launch application UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void seedDemoOrders(OrderProcessingSystem system) {
        new Thread(() -> {
            try {
                Thread.sleep(800);
                system.addOrder(new Order(
                        system.generateNextOrderId(),
                        "Tony Stark",
                        Arrays.asList("Pepperoni Pizza", "Cold Soda"),
                        true // VIP
                ));

                Thread.sleep(600);
                system.addOrder(new Order(
                        system.generateNextOrderId(),
                        "Peter Parker",
                        Arrays.asList("Double Cheeseburger", "Crispy Fries"),
                        false
                ));

                Thread.sleep(1200);
                system.addOrder(new Order(
                        system.generateNextOrderId(),
                        "Bruce Wayne",
                        Arrays.asList("Dragon Sushi Roll", "Matcha Tea"),
                        true // VIP
                ));
            } catch (InterruptedException ignored) {
            }
        }, "Demo-Seeder").start();
    }

    private static void printAsciiBanner() {
        System.out.println("================================================================================");
        System.out.println("  ███████╗ ██████╗  ██████╗ ██████╗ ███████╗██╗      ██████╗ ██╗    ██╗");
        System.out.println("  ██╔════╝██╔═══██╗██╔═══██╗██╔══██╗██╔════╝██║     ██╔═══██╗██║    ██║");
        System.out.println("  █████╗  ██║   ██║██║   ██║██║  ██║█████╗  ██║     ██║   ██║██║ █╗ ██║");
        System.out.println("  ██╔══╝  ██║   ██║██║   ██║██║  ██║██╔══╝  ██║     ██║   ██║██║███╗██║");
        System.out.println("  ██║     ╚██████╔╝╚██████╔╝██████╔╝██║     ███████╗╚██████╔╝╚███╔███╔╝");
        System.out.println("  ╚═╝      ╚═════╝  ╚═════╝ ╚═════╝ ╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝ ");
        System.out.println("      ONLINE FOOD ORDER PROCESSING SIMULATOR [NEOBRUTALISM UI/UX]");
        System.out.println("================================================================================");
        System.out.println("  [Architecture] : Java Multithreading & OOPS Concurrency");
        System.out.println("  [Workers]      : KitchenWorker -> PackagingWorker -> DeliveryWorker");
        System.out.println("  [Queues]       : Thread-Safe PriorityBlockingQueue Pipeline");
        System.out.println("  [UI Style]     : High-Contrast Neobrutalism (Hard Shadows, Chunky Buttons)");
        System.out.println("================================================================================\n");
    }
}
