package com.campus.marketplace.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class ModernTheme {
    public static final Color BG_DARK = new Color(18, 18, 20);
    public static final Color SURFACE = new Color(24, 24, 27);
    public static final Color CARD_BG = new Color(32, 32, 36);
    public static final Color CARD_HOVER = new Color(42, 42, 48);
    public static final Color ACCENT_ORANGE = new Color(242, 100, 64);
    public static final Color ACCENT_ORANGE_HOVER = new Color(224, 83, 48);
    public static final Color TEXT_PRIMARY = new Color(244, 244, 245);
    public static final Color TEXT_MUTED = new Color(161, 161, 170);
    public static final Color TEXT_DIM = new Color(113, 113, 122);
    public static final Color BORDER = new Color(46, 46, 51);

    public static final Color BADGE_GREEN = new Color(74, 222, 128);
    public static final Color BADGE_YELLOW = new Color(251, 191, 36);
    public static final Color BADGE_BLUE = new Color(96, 165, 250);
    public static final Color BADGE_RED = new Color(239, 68, 68);

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);

    public static void stylePrimaryButton(JButton btn) {
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_ORANGE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(CARD_BG);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> box) {
        box.setFont(FONT_BODY);
        box.setForeground(TEXT_PRIMARY);
        box.setBackground(CARD_BG);
        box.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(SURFACE);
        table.setGridColor(BORDER);
        table.setRowHeight(36);
        table.setSelectionBackground(CARD_HOVER);
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setForeground(TEXT_PRIMARY);
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(100, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }
}
