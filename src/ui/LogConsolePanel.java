package ui;

import utils.Logger;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * Neobrutalism Terminal Log Console.
 * Displays real-time synchronized thread logs directly from the backend Logger.
 */
public class LogConsolePanel extends JPanel {

    private JTextPane textPane;
    private StyledDocument doc;
    private JCheckBox autoScrollCheckBox;

    // Color attributes for log levels
    private final SimpleAttributeSet attrDefault = new SimpleAttributeSet();
    private final SimpleAttributeSet attrInfo = new SimpleAttributeSet();
    private final SimpleAttributeSet attrSuccess = new SimpleAttributeSet();
    private final SimpleAttributeSet attrWarn = new SimpleAttributeSet();
    private final SimpleAttributeSet attrError = new SimpleAttributeSet();
    private final SimpleAttributeSet attrWorker = new SimpleAttributeSet();

    public LogConsolePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(NeobrutalismTheme.COLOR_BLACK);
        setBorder(new LineBorder(NeobrutalismTheme.COLOR_BLACK, 3, false));

        setupStyles();
        buildHeader();
        buildConsole();

        // Subscribe to Logger stream
        Logger.addLogListener(this::appendLogEntry);
    }

    private void setupStyles() {
        StyleConstants.setFontFamily(attrDefault, "Courier New");
        StyleConstants.setFontSize(attrDefault, 12);
        StyleConstants.setForeground(attrDefault, new Color(0xDD, 0xDD, 0xDD));

        StyleConstants.setFontFamily(attrInfo, "Courier New");
        StyleConstants.setFontSize(attrInfo, 12);
        StyleConstants.setForeground(attrInfo, new Color(0x00, 0xF0, 0xFF)); // Cyan

        StyleConstants.setFontFamily(attrSuccess, "Courier New");
        StyleConstants.setFontSize(attrSuccess, 12);
        StyleConstants.setBold(attrSuccess, true);
        StyleConstants.setForeground(attrSuccess, new Color(0x00, 0xFF, 0x66)); // Bright Green

        StyleConstants.setFontFamily(attrWarn, "Courier New");
        StyleConstants.setFontSize(attrWarn, 12);
        StyleConstants.setForeground(attrWarn, new Color(0xFF, 0xE6, 0x00)); // Yellow

        StyleConstants.setFontFamily(attrError, "Courier New");
        StyleConstants.setFontSize(attrError, 12);
        StyleConstants.setBold(attrError, true);
        StyleConstants.setForeground(attrError, new Color(0xFF, 0x33, 0x66)); // Coral Red

        StyleConstants.setFontFamily(attrWorker, "Courier New");
        StyleConstants.setFontSize(attrWorker, 12);
        StyleConstants.setForeground(attrWorker, new Color(0xFF, 0xA5, 0x00)); // Orange
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x1A, 0x1A, 0x1A));
        header.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel title = new JLabel("[LOGS] LIVE THREAD EXECUTION LOGS");
        title.setFont(NeobrutalismTheme.FONT_SUBHEADER);
        title.setForeground(NeobrutalismTheme.COLOR_WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);

        autoScrollCheckBox = new JCheckBox("AUTO-SCROLL", true);
        autoScrollCheckBox.setFont(NeobrutalismTheme.FONT_SMALL);
        autoScrollCheckBox.setForeground(NeobrutalismTheme.COLOR_WHITE);
        autoScrollCheckBox.setOpaque(false);
        autoScrollCheckBox.setFocusPainted(false);
        controls.add(autoScrollCheckBox);

        JButton clearBtn = new JButton("CLEAR");
        clearBtn.setFont(NeobrutalismTheme.FONT_SMALL);
        clearBtn.setBackground(NeobrutalismTheme.COLOR_BLACK);
        clearBtn.setForeground(NeobrutalismTheme.COLOR_WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorder(new CompoundBorder(
                new LineBorder(NeobrutalismTheme.COLOR_WHITE, 1, false),
                new EmptyBorder(2, 6, 2, 6)
        ));
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clear());
        controls.add(clearBtn);

        header.add(controls, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildConsole() {
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(0x0C, 0x0C, 0x0C));
        textPane.setForeground(new Color(0xEE, 0xEE, 0xEE));
        textPane.setFont(new Font("Courier New", Font.PLAIN, 12));
        textPane.setBorder(new EmptyBorder(6, 8, 6, 8));
        doc = textPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void appendLogEntry(Logger.LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            try {
                SimpleAttributeSet attr;
                switch (entry.getLevel()) {
                    case SUCCESS:
                        attr = attrSuccess;
                        break;
                    case WARN:
                        attr = attrWarn;
                        break;
                    case ERROR:
                        attr = attrError;
                        break;
                    case WORKER:
                        attr = attrWorker;
                        break;
                    case INFO:
                        attr = attrInfo;
                        break;
                    default:
                        attr = attrDefault;
                        break;
                }

                doc.insertString(doc.getLength(), entry.getFormatted() + "\n", attr);

                // Trim buffer if too long (> 2000 lines)
                if (doc.getLength() > 80000) {
                    doc.remove(0, 20000);
                }

                if (autoScrollCheckBox.isSelected()) {
                    textPane.setCaretPosition(doc.getLength());
                }
            } catch (BadLocationException ignored) {
            }
        });
    }

    public void clear() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }
}
