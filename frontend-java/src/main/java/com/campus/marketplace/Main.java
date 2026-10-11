package com.campus.marketplace;

import com.campus.marketplace.ui.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Set system properties for HiDPI scaling and smooth text rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize modern FlatLaf dark look and feel
                FlatDarkLaf.setup();
                
                // Customize global UI defaults to match Study Essentials theme
                UIManager.put("Panel.background", new Color(18, 18, 20));
                UIManager.put("Button.arc", 8);
                UIManager.put("Component.arc", 8);
                UIManager.put("ScrollBar.thumbArc", 8);
                UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
                UIManager.put("Table.selectionBackground", new Color(42, 42, 48));
                UIManager.put("Table.selectionForeground", new Color(244, 244, 245));
                UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

            } catch (Exception e) {
                System.err.println("Could not load FlatLaf: " + e.getMessage() + ". Using system L&F.");
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
