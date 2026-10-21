# 🍔 FOODFLOW // Online Food Order Processing Simulator

> **A High-Performance Multi-Threaded Order Processing Simulator built in Java with Neobrutalism UI/UX Architecture.**  
> Perfect demonstration of **Object-Oriented Programming (OOPS)**, **Thread Synchronization**, **Producer-Consumer Queues**, and **Reactive GUI Architecture**.

---

## 📸 Overview & Architecture

FOODFLOW models an industrial online food delivery dispatch system where concurrent worker threads coordinate orders smoothly across three sequential processing stages:
1. **🍳 Kitchen Stage** (Food preparation & cooking)
2. **📦 Packaging Stage** (Quality inspection, boxing & sealing)
3. **🚀 Delivery Stage** (Courier dispatch & customer transit)

```
                       +------------------------------------------------------+
                       |            CLIENT / ORDER DISPATCH DECK              |
                       |       (OrderInputPanel / Batch Simulation)           |
                       +------------------------------------------------------+
                                                  |
                                            offer(Order)
                                                  v
                   +--------------------------------------------------------------+
                   |          [KITCHEN QUEUE] PriorityBlockingQueue<Order>        |
                   |               (VIP Orders Jump to Front of Queue)            |
                   +--------------------------------------------------------------+
                                                  |
                                             poll(Order)
                                                  v
                   +--------------------------------------------------------------+
                   |                 THREAD 1: KitchenWorker                      |
                   |             (Simulated cooking: 2.0s - 5.0s)                 |
                   |          PENDING  -->  PREPARING  -->  PREPARED              |
                   +--------------------------------------------------------------+
                                                  |
                                            put(Order)
                                                  v
                   +--------------------------------------------------------------+
                   |         [PACKAGING QUEUE] PriorityBlockingQueue<Order>       |
                   +--------------------------------------------------------------+
                                                  |
                                             poll(Order)
                                                  v
                   +--------------------------------------------------------------+
                   |                THREAD 2: PackagingWorker                     |
                   |             (Simulated boxing: 1.0s - 3.0s)                  |
                   |          PREPARED  -->  PACKAGING  -->  PACKAGED             |
                   +--------------------------------------------------------------+
                                                  |
                                            put(Order)
                                                  v
                   +--------------------------------------------------------------+
                   |         [DELIVERY QUEUE] PriorityBlockingQueue<Order>        |
                   +--------------------------------------------------------------+
                                                  |
                                             poll(Order)
                                                  v
                   +--------------------------------------------------------------+
                   |                 THREAD 3: DeliveryWorker                     |
                   |             (Simulated transit: 1.0s - 2.0s)                 |
                   |          PACKAGED  -->  DELIVERING  -->  DELIVERED           |
                   +--------------------------------------------------------------+
                                                  |
                                            orderCompleted()
                                                  v
     +------------------------------------------------------------------------------------------+
     |                    EVENT DISPATCH BUS (OrderUpdateListener / Observer)                   |
     |         --> Real-Time Neobrutalism 3-Lane Dashboard Card Transitions                     |
     |         --> Atomic Performance Statistics & Telemetry Dashboard                          |
     |         --> Thread State & Resource Contention Monitor                                   |
     |         --> Pure Java Audio Synthesizer (Upbeat Completion Chime)                        |
     |         --> Completed Orders Audit Table & One-Click CSV Export                          |
     +------------------------------------------------------------------------------------------+
```

---

## ⚡ Concurrency & OOPS Concepts Demonstrated

### 1. Object-Oriented Principles (OOPS)
- **Encapsulation**: `Order` and `SystemStatistics` enforce strict private variables with guarded accessors, input validation, and defensive copying of mutable lists (`Collections.unmodifiableList`).
- **Polymorphism & Abstraction**: `WorkerThread` interface abstracts worker lifecycle controls (`pauseWorker()`, `resumeWorker()`, `shutdown()`, inspection metrics) while `KitchenWorker`, `PackagingWorker`, and `DeliveryWorker` provide stage-specific behaviors.
- **Observer Pattern**: `OrderUpdateListener` completely decouples backend worker threads from the Swing presentation layer.
- **Natural Ordering**: `Order` implements `Comparable<Order>` prioritizing VIP orders first, followed by FIFO submission timestamp ordering.

### 2. Multi-Threading & Concurrency
- **Coordinated Worker Threads**: Dedicated threads running independently in a managed `ExecutorService` thread pool.
- **Producer-Consumer Architecture**: Standardized handoffs between pipeline stages using `PriorityBlockingQueue<Order>`, preventing race conditions without bulky manual lock boilerplate.
- **Thread-Safe Telemetry**: Metric calculations powered by atomic primitives (`AtomicInteger`, `AtomicLong`) ensuring zero deadlocks and zero data races.
- **Thread-Safe Logging**: Centralized synchronized logger ensuring output lines never interleave, streaming live entries to a real-time monospace UI terminal console.
- **Graceful Thread Interruption**: Workers respond to thread interrupts, timeouts (`poll(500, TimeUnit.MILLISECONDS)`), and pause signals safely at deterministic checkpoints.
- **UI Responsiveness (No Freezing)**: All UI state changes are safely dispatched via `SwingUtilities.invokeLater(...)`, guaranteeing standard Swing single-thread rule adherence.

---

## 🎨 Neobrutalism UI/UX Design System

FOODFLOW adopts the modern **Neobrutalism** aesthetic:
- **Bold 3px - 5px solid black borders** (`#000000`)
- **Hard offset drop shadows** (4px 4px 0px black rectangles)
- **High contrast vibrant palette**:
  - `Electric Blue` (#0066FF) - Cooking stage
  - `Neon Yellow` (#FFE600) - Packaging stage
  - `Acid Green` (#00E676) - Delivery & Completed stage
  - `Hot Coral/Pink` (#FF3366) - System Pause & Cancel alerts
  - `Cyber Cyan` (#00F0FF) - Telemetry & Batch Simulators
  - `Neon Purple` (#B5179E) - VIP priority orders
- **Sharp geometric angles** (0px border radius, crisp square edges)
- **Physical button tactile feedback** (shifts down-right upon mouse press)
- **Monospace Tech Typography** (`Courier New` / bold weights)

---

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| **3-Lane Processing Dashboard** | Live cards dynamically travel left-to-right across KITCHEN, PACKAGING, and DELIVERY lanes |
| **Real-Time Stage Progress Bars** | Linear progress bars with elapsed time counters tick every 100ms |
| **Thread Monitor Panel** | Live status indicators for `KitchenWorker-1`, `PackagingWorker-1`, `DeliveryWorker-1` (RUNNING / IDLE / PAUSED) |
| **VIP Priority Orders** | VIP orders jump ahead in the priority queues and cook with higher kitchen priority |
| **One-Click Batch Simulation** | Instantly enqueue 5 randomized orders to stress-test concurrent queue handoffs |
| **System Pause / Resume** | Freeze and resume all background worker threads dynamically |
| **Live Thread Console Terminal** | Color-coded synchronized terminal log showing millisecond timestamps and thread IDs |
| **Audit Log & History Table** | Searchable table of completed orders with individual kitchen, packaging, and delivery durations |
| **CSV Export** | Export the entire order audit log to a CSV report with one click |
| **Audio Feedback** | Synthesized arcade tones and chimes using pure standard Java sound synthesis (no external MP3/WAV files) |

---

## 🛠️ Project Structure

```
FOODFLOW/
├── src/
│   ├── models/
│   │   ├── Order.java               # Encapsulated Order model with VIP priority & timings
│   │   └── OrderStatus.java         # Lifecycle enum with state metadata & hex colors
│   ├── threading/
│   │   ├── WorkerThread.java        # Worker contract interface for metrics & controls
│   │   ├── KitchenWorker.java       # Food preparation thread (2.0s - 5.0s delay)
│   │   ├── PackagingWorker.java     # Packaging and boxing thread (1.0s - 3.0s delay)
│   │   └── DeliveryWorker.java      # Dispatch courier thread (1.0s - 2.0s delay)
│   ├── core/
│   │   ├── OrderProcessingSystem.java # Main pipeline orchestrator & thread pool manager
│   │   ├── OrderUpdateListener.java   # Event listener interface (Observer pattern)
│   │   └── SystemStatistics.java      # Atomic counters for throughput telemetry
│   ├── utils/
│   │   ├── Logger.java              # Thread-safe synchronized logger & UI subscriber
│   │   └── SoundManager.java        # Audio tone synthesizer for order notifications
│   ├── ui/
│   │   ├── MainWindow.java          # Primary application window & layout coordinator
│   │   ├── DashboardLanePanel.java  # Neobrutalist vertical lane component
│   │   ├── OrderCard.java           # Order card with linear progress bar & timers
│   │   ├── StatsPanel.java          # 6-metric telemetry bar with big bold numbers
│   │   ├── ThreadMonitorPanel.java  # Thread state and throughput inspector
│   │   ├── OrderInputPanel.java     # Order form, quick-add food chips, & batch runner
│   │   ├── HistoryTablePanel.java   # Completed orders table, filter & CSV export
│   │   ├── LogConsolePanel.java     # Color-coded live monospace log console
│   │   └── NeobrutalismTheme.java   # Design tokens, hard shadows, & chunky buttons
│   └── Main.java                    # Application bootstrap & initial demo seeder
├── build.bat                        # Windows 1-click build & JAR packaging script
├── run.bat                          # Windows 1-click execution script
├── FoodFlow.jar                     # Standalone executable JAR file
└── README.md                        # Documentation & Architecture report
```

---

## 💻 How to Build and Run

### Prerequisites
- **Java SE Development Kit (JDK) 11+** (Tested and verified on Java 17 LTS).
- No external libraries, Maven, or Gradle required — uses 100% pure standard Java SE.

### Option 1: Quick Run (Using Pre-built Executable JAR)
```bash
# Double-click run.bat OR execute in terminal:
java -jar FoodFlow.jar
```

### Option 2: Build and Run from Source
```bash
# Compile all source classes into bin directory
javac -encoding UTF-8 -d bin src/models/*.java src/threading/*.java src/utils/*.java src/core/*.java src/ui/*.java src/Main.java

# Run directly
java -cp bin Main

# Or package as executable JAR
jar cfe FoodFlow.jar Main -C bin .
java -jar FoodFlow.jar
```

---

## 📋 Evaluation & Verification Checklist

- [x] **Proper OOPS Implementation**: Encapsulation, defensive copying, immutability, enum state machine.
- [x] **Separate Worker Threads**: Dedicated `KitchenWorker`, `PackagingWorker`, and `DeliveryWorker`.
- [x] **Thread Coordination**: Non-blocking handoff using `PriorityBlockingQueue` and `ExecutorService`.
- [x] **Neobrutalism UI/UX**: 3-5px solid black borders, hard offset shadows, high-contrast palette.
- [x] **3-Lane Processing Dashboard**: Left-to-right animated transitions across stages.
- [x] **Real-time Live Telemetry**: Metrics panel with big bold numbers and queue counters.
- [x] **Thread-Safe Logging**: Synchronized console logs mirrored to a live UI terminal.
- [x] **Standalone Executable**: Packaged `FoodFlow.jar` and 1-click `run.bat` scripts.

---

## 📜 License
MIT License. Built for coursework demonstration and portfolio showcase.
