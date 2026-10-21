package utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Audio feedback utility providing Neobrutalist arcade sound cues
 * using pure standard Java sound synthesis (no external MP3/WAV dependencies).
 */
public final class SoundManager {

    private static volatile boolean soundEnabled = true;
    private static final ExecutorService SOUND_POOL = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Audio-Synthesizer");
        t.setDaemon(true);
        return t;
    });

    private SoundManager() {
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static void toggleSound() {
        soundEnabled = !soundEnabled;
    }

    /**
     * Synthesizes a bright multi-tone chime when an order is completed.
     */
    public static void playOrderCompletedSound() {
        if (!soundEnabled) return;

        SOUND_POOL.submit(() -> {
            try {
                // Play a bright upbeat 3-tone chime (523Hz C5 -> 659Hz E5 -> 784Hz G5)
                playTone(523, 60, 0.4);
                Thread.sleep(30);
                playTone(659, 60, 0.4);
                Thread.sleep(30);
                playTone(784, 120, 0.5);
            } catch (Exception e) {
                // Fallback to system toolkit beep
                try {
                    Toolkit.getDefaultToolkit().beep();
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Synthesizes an energetic 'punch' tone when a new order is dispatched.
     */
    public static void playOrderAddedSound() {
        if (!soundEnabled) return;

        SOUND_POOL.submit(() -> {
            try {
                playTone(440, 50, 0.3);
                playTone(880, 70, 0.4);
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Synthesizes an audio sine wave tone at specified frequency and duration.
     */
    private static void playTone(int frequency, int durationMs, double volume) {
        try {
            float sampleRate = 8000f;
            byte[] buf = new byte[(int) (sampleRate * durationMs / 1000)];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                buf[i] = (byte) (Math.sin(angle) * 127.0 * volume);
            }

            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
            }
        } catch (Exception e) {
            // Audio line unavailable in some headless environments
        }
    }
}
