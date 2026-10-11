package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginBtn = new JButton("Log In to Marketplace");
    private final JButton registerBtn = new JButton("Create New Student Account");
    private final JLabel statusLabel = new JLabel(" ");

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(32, 36, 32, 36)
        ));
        card.setPreferredSize(new Dimension(420, 520));

        // Logo & Title
        JLabel brandLabel = new JLabel("STUDY ESSENTIALS");
        brandLabel.setFont(ModernTheme.FONT_HEADER);
        brandLabel.setForeground(ModernTheme.ACCENT_ORANGE);
        brandLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(brandLabel);

        JLabel subLabel = new JLabel("Peer-to-Peer Campus Marketplace");
        subLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        subLabel.setForeground(ModernTheme.TEXT_MUTED);
        subLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(subLabel);
        card.add(Box.createVerticalStrut(24));

        // Email Field
        JLabel emailLbl = new JLabel("College Email Address");
        emailLbl.setFont(ModernTheme.FONT_BODY_BOLD);
        emailLbl.setForeground(ModernTheme.TEXT_PRIMARY);
        emailLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(emailLbl);
        card.add(Box.createVerticalStrut(4));

        ModernTheme.styleTextField(emailField);
        emailField.setAlignmentX(LEFT_ALIGNMENT);
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));

        // Password Field
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(ModernTheme.FONT_BODY_BOLD);
        passLbl.setForeground(ModernTheme.TEXT_PRIMARY);
        passLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(passLbl);
        card.add(Box.createVerticalStrut(4));

        ModernTheme.styleTextField(passwordField);
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(16));

        // Login Button
        ModernTheme.stylePrimaryButton(loginBtn);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(8));

        // Register Switch Button
        ModernTheme.styleSecondaryButton(registerBtn);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> mainFrame.showScreen("REGISTER"));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));

        // Quick Demo Accounts Box
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setBackground(ModernTheme.CARD_BG);
        demoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        demoBox.setAlignmentX(CENTER_ALIGNMENT);

        JLabel demoTitle = new JLabel("⚡ Quick Viva Demo Presets:");
        demoTitle.setFont(ModernTheme.FONT_SMALL_BOLD);
        demoTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        demoBox.add(demoTitle);
        demoBox.add(Box.createVerticalStrut(4));

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        demoBtns.setOpaque(false);

        JButton fillStudent = new JButton("Student (Atharva)");
        fillStudent.setFont(ModernTheme.FONT_SMALL);
        fillStudent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fillStudent.addActionListener(e -> {
            emailField.setText("atharva@campus.edu");
            passwordField.setText("Student@123");
        });
        demoBtns.add(fillStudent);

        JButton fillAdmin = new JButton("Admin (Prof. Sharma)");
        fillAdmin.setFont(ModernTheme.FONT_SMALL);
        fillAdmin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fillAdmin.addActionListener(e -> {
            emailField.setText("admin@campus.edu");
            passwordField.setText("Admin@123");
        });
        demoBtns.add(fillAdmin);

        JButton fillPending = new JButton("Pending Student (Rohit)");
        fillPending.setFont(ModernTheme.FONT_SMALL);
        fillPending.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fillPending.addActionListener(e -> {
            emailField.setText("rohit@campus.edu");
            passwordField.setText("Student@123");
        });
        demoBtns.add(fillPending);

        demoBox.add(demoBtns);
        card.add(demoBox);
        card.add(Box.createVerticalStrut(10));

        statusLabel.setFont(ModernTheme.FONT_SMALL);
        statusLabel.setForeground(ModernTheme.BADGE_RED);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(statusLabel);

        add(card, new GridBagConstraints());
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        loginBtn.setEnabled(false);
        statusLabel.setForeground(ModernTheme.BADGE_BLUE);
        statusLabel.setText("Authenticating with campus server...");

        SwingWorkerHelper.execute(() -> AuthService.login(email, password), user -> {
            loginBtn.setEnabled(true);
            statusLabel.setText("");
            mainFrame.onUserLoggedIn(user);
        }, error -> {
            loginBtn.setEnabled(true);
            statusLabel.setForeground(ModernTheme.BADGE_RED);
            statusLabel.setText(error.getMessage());
        });
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
    }
}
