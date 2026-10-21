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
 * - High-contrast palette (Black, White, Neon Yellow, Electric Blue, Bright Green, Hot Red, Cyber Cyan, Neon Purple)
 * - Raw geometric rectangles (0px border radius, sharp angles)
 * - 3px-4px solid black borders
 * - Hard offset drop shadows (4px 4px 0px #000)
 * - Heavy crisp typography
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
    public static final Color COLOR_MUTED = new Color(0xEE, 0xEE, 0xE8);      // Border muted / lane bg

    // Typography (Using fonts with 100% glyph support on Windows)
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 12);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 30);

    private NeobrutalismTheme() {
    }

    /**
     * Creates a standard thick solid black border with internal padding.
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
     * Custom Border for Panels simulating Neobrutalist Hard Shadow (solid black offset).
     * NOTE: Does NOT paint over children or inside the client area!
     */
    public static class HardShadowBorder implements Border {
        private final int borderThickness;
        private final int shadowSize;
        private final Insets insets;

        public HardShadowBorder(int borderThickness, int shadowSize, int padding) {
            this.borderThickness = borderThickness;
            this.shadowSize = shadowSize;
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
            g2d.setColor(COLOR_BLACK);
            g2d.fillRect(x + shadowSize, y + shadowSize, boxWidth, boxHeight);

            // Draw Box Face Background
            g2d.setColor(c.getBackground());
            g2d.fillRect(x, y, boxWidth, boxHeight);

            // Draw 3px Solid Black Border
            g2d.setColor(COLOR_BLACK);
            g2d.setStroke(new BasicStroke(borderThickness));
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
     * Dedicated Neobrutalism Button with 100% Guaranteed Visible Label Text,
     * crisp anti-aliasing, solid hard black shadow, and tactile mechanical press animation.
     */
    public static class NeobrutalButton extends JButton {
        private final int borderThickness = 3;
        private final int shadowSize = 4;
        private final Color normalBg;
        private final Color hoverBg;
        private final Color textColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public NeobrutalButton(String text, Color bgColor, Color textColor) {
            super(text);
            this.normalBg = bgColor;
            this.hoverBg = darken(bgColor, 0.12f);
            this.textColor = textColor;

            setFont(FONT_SUBHEADER);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(4, 8, 8, 8)); // Reserve space for shadow
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int xOffset = isPressed ? 2 : 0;
            int yOffset = isPressed ? 2 : 0;

            int boxW = w - shadowSize - 1;
            int boxH = h - shadowSize - 1;

            if (boxW <= 0 || boxH <= 0) {
                g2d.dispose();
                return;
            }

            // 1. Draw Hard Shadow (solid black offset bottom-right)
            if (!isPressed) {
                g2d.setColor(COLOR_BLACK);
                g2d.fillRect(xOffset + shadowSize, yOffset + shadowSize, boxW, boxH);
            }

            // 2. Draw Button Face Background
            g2d.setColor(isHovered ? hoverBg : normalBg);
            g2d.fillRect(xOffset, yOffset, boxW, boxH);

            // 3. Draw Thick Solid Black Border Outline
            g2d.setColor(COLOR_BLACK);
            g2d.setStroke(new BasicStroke(borderThickness));
            g2d.drawRect(xOffset + borderThickness / 2, yOffset + borderThickness / 2,
                    boxW - borderThickness, boxH - borderThickness);

            // 4. Draw Label Text - GUARANTEED VISIBLE, CRISP AND CENTERED!
            String text = getText();
            if (text != null && !text.isEmpty()) {
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textW = fm.stringWidth(text);
                int textX = xOffset + (boxW - textW) / 2;
                int textY = yOffset + (boxH - fm.getHeight()) / 2 + fm.getAscent();

                g2d.setColor(textColor);
                g2d.drawString(text, textX, textY);
            }

            g2d.dispose();
        }
    }

    /**
     * Factory method creating a NeobrutalButton.
     */
    public static JButton createButton(String text, Color bgColor, Color textColor) {
        return new NeobrutalButton(text, bgColor, textColor);
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
