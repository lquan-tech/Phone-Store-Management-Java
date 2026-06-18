package com.quanlycuahang.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Class chua cac phuong thuc tien ich de thiet ke giao dien Swing dong nhat.
 */
public class UIUtils {

    // ================================================================
    // COLOR PALETTE
    // ================================================================
    public static final Color PRIMARY       = new Color(37, 99, 235);    // Blue-600
    public static final Color PRIMARY_DARK  = new Color(29, 78, 216);    // Blue-700
    public static final Color SUCCESS       = new Color(22, 163, 74);    // Green-600
    public static final Color DANGER        = new Color(220, 38, 38);    // Red-600
    public static final Color WARNING       = new Color(217, 119, 6);    // Amber-600
    public static final Color INFO          = new Color(8, 145, 178);    // Cyan-600

    public static final Color BG_DARK       = new Color(244, 247, 251);  // App background
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color BG_PANEL      = new Color(232, 238, 247);
    public static final Color BG_INPUT      = Color.WHITE;
    public static final Color BG_TABLE_ROW  = Color.WHITE;
    public static final Color BG_TABLE_ALT  = new Color(248, 250, 252);
    public static final Color SIDEBAR_BG    = new Color(250, 252, 255);
    public static final Color NAV_ACTIVE_BG = new Color(232, 240, 254);

    public static final Color TEXT_PRIMARY   = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    public static final Color TEXT_MUTED     = new Color(100, 116, 139);
    public static final Color BORDER_COLOR   = new Color(203, 213, 225);

    // ================================================================
    // FONTS
    // ================================================================
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 13);

    // ================================================================
    // Button Factory
    // ================================================================

    /** Tao nut bam theo mau sac va style chuan */
    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bgColor);
        btn.setForeground(isLight(bgColor) ? TEXT_PRIMARY : Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);

        // Hover effect
        Color hoverColor = isLight(bgColor) ? new Color(218, 226, 238) : bgColor.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    public static JButton createPrimaryButton(String text) { return createButton(text, PRIMARY); }
    public static JButton createSuccessButton(String text) { return createButton(text, SUCCESS); }
    public static JButton createDangerButton(String text)  { return createButton(text, DANGER); }
    public static JButton createWarningButton(String text) { return createButton(text, WARNING); }
    public static JButton createInfoButton(String text)    { return createButton(text, INFO); }

    private static boolean isLight(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance > 0.72;
    }

    // ================================================================
    // TextField / ComboBox Factory
    // ================================================================

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JTextField createSearchField(String placeholder) {
        JTextField tf = createTextField();
        tf.setPreferredSize(new Dimension(220, 36));
        tf.setText(placeholder);
        tf.setForeground(TEXT_MUTED);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(TEXT_MUTED);
                }
            }
        });
        return tf;
    }

    public static void addDebouncedTextChangeListener(JTextField field, int delayMs, Runnable action) {
        Timer timer = new Timer(delayMs, e -> action.run());
        timer.setRepeats(false);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { timer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { timer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { timer.restart(); }
        });
    }

    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        styleComboBox(cb);
        return cb;
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }

    // ================================================================
    // Label Factory
    // ================================================================

    public static JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ================================================================
    // Table Styling
    // ================================================================

    /** Ap dung style dep cho JTable */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setBackground(BG_TABLE_ROW);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Alternate row color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                setFont(FONT_TABLE);
                setBorder(new EmptyBorder(0, 12, 0, 8));
                if (isSelected) {
                    setBackground(PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? BG_TABLE_ROW : BG_TABLE_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                if (col >= 3) setHorizontalAlignment(SwingConstants.RIGHT);
                else setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        });

        // Style header — su dung custom renderer de dam bao mau hien thi
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(BG_PANEL);
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    t, val, isSelected, hasFocus, row, col);
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(TEXT_PRIMARY);
                lbl.setBackground(BG_PANEL);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 12, 0, 8));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
    }

    /** Boc JTable trong JScrollPane dep */
    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_TABLE_ROW);
        sp.setBackground(BG_CARD);
        return sp;
    }

    // ================================================================
    // Panel / Card Factory
    // ================================================================

    /** Tao panel card co border va padding */
    public static JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 16, 16, 16)
        ));
        if (title != null && !title.isEmpty()) {
            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(FONT_HEADING);
            titleLbl.setForeground(TEXT_PRIMARY);
            titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));
            card.add(titleLbl, BorderLayout.NORTH);
        }
        return card;
    }

    public static JPanel createStatCard(String title, String value, Color accent) {
        JPanel card = createCard(null);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel titleLabel = createLabel(title);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public static JLabel createBadge(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(color);
        label.setOpaque(true);
        label.setBackground(new Color(
            Math.min(255, color.getRed() + 210),
            Math.min(255, color.getGreen() + 210),
            Math.min(255, color.getBlue() + 210)
        ));
        label.setBorder(new EmptyBorder(5, 10, 5, 10));
        return label;
    }

    // ================================================================
    // Separator
    // ================================================================

    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        return sep;
    }

    // ================================================================
    // Set LAF + global defaults
    // ================================================================

    public static void applyGlobalUI() {
        try {
            // Dung Cross-Platform (Metal) de mau custom hoat dong chinh xac
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { /* dung mac dinh */ }

        // Panel / background
        UIManager.put("Panel.background",               BG_DARK);
        UIManager.put("Panel.foreground",               TEXT_PRIMARY);

        // TabbedPane
        UIManager.put("TabbedPane.background",          BG_CARD);
        UIManager.put("TabbedPane.foreground",          TEXT_PRIMARY);   // chu tab
        UIManager.put("TabbedPane.selected",            BG_CARD);
        UIManager.put("TabbedPane.selectedForeground",  TEXT_PRIMARY);
        UIManager.put("TabbedPane.unselectedBackground", BG_DARK);
        UIManager.put("TabbedPane.shadow",              BORDER_COLOR);
        UIManager.put("TabbedPane.darkShadow",          BG_DARK);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabAreaBackground",   BG_DARK);
        UIManager.put("TabbedPane.font",                FONT_HEADING);

        // Table
        UIManager.put("Table.background",               BG_TABLE_ROW);
        UIManager.put("Table.foreground",               TEXT_PRIMARY);
        UIManager.put("Table.gridColor",                BORDER_COLOR);
        UIManager.put("TableHeader.background",         BG_PANEL);
        UIManager.put("TableHeader.foreground",         TEXT_PRIMARY);
        UIManager.put("TableHeader.font",               FONT_HEADER);

        // ScrollBar
        UIManager.put("ScrollBar.thumb",                BG_PANEL);
        UIManager.put("ScrollBar.track",                BG_DARK);

        // OptionPane / Dialog
        UIManager.put("OptionPane.background",          BG_CARD);
        UIManager.put("OptionPane.foreground",          TEXT_PRIMARY);
        UIManager.put("OptionPane.messageForeground",   TEXT_PRIMARY);

        // Button
        UIManager.put("Button.background",              PRIMARY);
        UIManager.put("Button.foreground",              Color.WHITE);
        UIManager.put("Button.select",                  PRIMARY_DARK);
        UIManager.put("Button.font",                    FONT_BUTTON);

        // TextField / Label
        UIManager.put("TextField.background",           BG_INPUT);
        UIManager.put("TextField.foreground",           TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",      TEXT_PRIMARY);
        UIManager.put("Label.foreground",               TEXT_PRIMARY);
        UIManager.put("Label.font",                     FONT_BODY);

        // ComboBox
        UIManager.put("ComboBox.background",            BG_INPUT);
        UIManager.put("ComboBox.foreground",            TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",   PRIMARY);
        UIManager.put("ComboBox.selectionForeground",   Color.WHITE);
    }

    // ================================================================
    // Dialog helpers
    // ================================================================

    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Loi", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Xac nhan",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
