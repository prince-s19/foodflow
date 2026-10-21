package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-Safe Centralized Logging Utility.
 * Features:
 * - Thread name and exact millisecond timestamp tags
 * - Synchronized console outputs ensuring logs do not interleave or corrupt
 * - Observer pattern for streaming logs directly to the Neobrutalism UI terminal
 */
public final class Logger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object LOCK = new Object();
    private static final List<Consumer<LogEntry>> LISTENERS = new CopyOnWriteArrayList<>();

    private Logger() {
        // Prevent instantiation
    }

    public enum Level {
        INFO, SUCCESS, WARN, ERROR, WORKER, SYSTEM
    }

    public static class LogEntry {
        private final String timestamp;
        private final String threadName;
        private final Level level;
        private final String message;

        public LogEntry(String timestamp, String threadName, Level level, String message) {
            this.timestamp = timestamp;
            this.threadName = threadName;
            this.level = level;
            this.message = message;
        }

        public String getTimestamp() { return timestamp; }
        public String getThreadName() { return threadName; }
        public Level getLevel() { return level; }
        public String getMessage() { return message; }

        public String getFormatted() {
            return String.format("[%s] [%-16s] [%-7s] %s", timestamp, threadName, level, message);
        }
    }

    public static void addLogListener(Consumer<LogEntry> listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void removeLogListener(Consumer<LogEntry> listener) {
        LISTENERS.remove(listener);
    }

    private static void log(Level level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        LogEntry entry = new LogEntry(timestamp, threadName, level, message);

        // Synchronize console output to avoid interleaved characters from concurrent threads
        synchronized (LOCK) {
            System.out.println(entry.getFormatted());
        }

        // Notify UI subscribers
        for (Consumer<LogEntry> listener : LISTENERS) {
            try {
                listener.accept(entry);
            } catch (Exception ignored) {
                // Ignore listener exceptions to preserve worker stability
            }
        }
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void success(String message) {
        log(Level.SUCCESS, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void worker(String message) {
        log(Level.WORKER, message);
    }

    public static void system(String message) {
        log(Level.SYSTEM, message);
    }
}
