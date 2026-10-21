package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Neobrutalism Theme & Design System Tokens.
 * Characteristics:
 * - High-contrast palette (Black, White, Neon Yellow, Electric Blue, Bright Green, Hot Red)
 * - Raw geometric rectangles (0px border radius, sharp angles)
 * - 3px-4px solid black borders
 * - Hard offset drop shadows (4px 4px 0px #000)
 * - Heavy monospace/bold typography
 */
public final class NeobrutalismTheme {

    // Color Palette
    public static final Color COLOR_BG = new Color(0xF4, 0xF4, 0xF0);         // Off-white / Cream
    public static final Color COLOR_PANEL_BG = new Color(0xFF, 0xFF, 0xFF);   // Pure White
    public static final Color COLOR_BLACK = new Color(0x00, 0x00, 0x00);      // Pure Black
    public static final Color COLOR_WHITE = new Color(0xFF, 0xFF, 0xFF);

    // Accent Colors
    public static final Color ACCENT_BLUE = new Color(0x00, 0x66, 0xFF);      // Electric Blue (Preparing)
    public static final Color ACCENT_YELLOW = new Color(0xFF, 0xE6, 0x00);    // Neon Yellow (Packaging)
    public static final Color ACCENT_GREEN = new Color(0x00, 0xE6, 0x76);     // Acid Green (Delivered)
    public static final Color ACCENT_RED = new Color(0xFF, 0x33, 0x66);       // Hot Pinkish-Red (Alert / Cancel)
    public static final Color ACCENT_PURPLE = new Color(0xB5, 0x17, 0x9E);    // Neon Purple (VIP)
    public static final Color ACCENT_ORANGE = new Color(0xFF, 0x7A, 0x00);    // Punchy Orange
    public static final Color ACCENT_CYAN = new Color(0x00, 0xF0, 0xFF);      // Cyber Cyan
    public static final Color COLOR_MUTED = new Color(0xE5, 0xE5, 0xDF);      // Border muted / lane bg

    // Typography
    public static final Font FONT_TITLE = new Font("Courier New", Font.BOLD, 26);
    public static final Font FONT_HEADER = new Font("Courier New", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Courier New", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Courier New", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Courier New", Font.BOLD, 11);
    public static final Font FONT_BIG_NUMBER = new Font("Courier New", Font.BOLD, 32);

    private NeobrutalismTheme() {
    }

    /**
     * Creates a standard 3px thick solid black border with internal padding.
     */
    public static Border createThickBorder(int thickness, int padding) {
        return new CompoundBorder(
                new LineBorder(COLOR_BLACK, thickness, false),
                new EmptyBorder(padding, padding, padding, padding)
        );
    }

    public static Border createThickBorder(int thickness) {
        return createThickBorder(thickness, 6);
    }

    /**
     * Custom Border simulating Neobrutalist Hard Shadow (solid black offset).
     */
    public static class HardShadowBorder implements Border {
        private final int borderThickness;
        private final int shadowSize;
        private final Color borderColor;
        private final Color shadowColor;
        private final Insets insets;

        public HardShadowBorder(int borderThickness, int shadowSize, int padding) {
            this.borderThickness = borderThickness;
            this.shadowSize = shadowSize;
            this.borderColor = COLOR_BLACK;
            this.shadowColor = COLOR_BLACK;
            this.insets = new Insets(
                    padding + borderThickness,
                    padding + borderThickness,
                    padding + borderThickness + shadowSize,
                    padding + borderThickness + shadowSize
            );
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int boxWidth = width - shadowSize;
            int boxHeight = height - shadowSize;

            // Draw Hard Shadow rectangle (offset bottom-right)
            g2d.setColor(shadowColor);
            g2d.fillRect(x + shadowSize, y + shadowSize, boxWidth, boxHeight);

            // Draw Foreground Box background
            g2d.setColor(c.getBackground());
            g2d.fillRect(x, y, boxWidth, boxHeight);

            // Draw Border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderThickness));
            // Inset by half stroke for crisp alignment
            g2d.drawRect(x + borderThickness / 2, y + borderThickness / 2,
                    boxWidth - borderThickness, boxHeight - borderThickness);

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    /**
     * Creates a chunky Neobrutalist button with solid border, hard shadow, and physical press feedback.
     */
    public static JButton createButton(String text, Color bgColor, Color textColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUBHEADER);
        btn.setBackground(bgColor);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new HardShadowBorder(3, 3, 6));

        // Hover & Press animation
        btn.addMouseListener(new MouseAdapter() {
            private final Color originalBg = bgColor;

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(darken(originalBg, 0.15f));
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg);
                btn.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBorder(new CompoundBorder(
                        new EmptyBorder(3, 3, 0, 0), // Shift down-right
                        new LineBorder(COLOR_BLACK, 3, false)
                ));
                btn.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBorder(new HardShadowBorder(3, 3, 6));
                btn.repaint();
            }
        });

        return btn;
    }

    public static Color darken(Color color, float fraction) {
        int red = Math.max(0, (int) (color.getRed() * (1 - fraction)));
        int green = Math.max(0, (int) (color.getGreen() * (1 - fraction)));
        int blue = Math.max(0, (int) (color.getBlue() * (1 - fraction)));
        return new Color(red, green, blue);
    }

    public static Color lighten(Color color, float fraction) {
        int red = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * fraction));
        int green = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * fraction));
        int blue = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * fraction));
        return new Color(red, green, blue);
    }
}
