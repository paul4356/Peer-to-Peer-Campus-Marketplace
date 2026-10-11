package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class RegisterPanel extends JPanel {
    private final MainFrame mainFrame;

    private final JTextField rollField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<String> deptBox = new JComboBox<>(new String[]{
            "Computer Engineering", "Information Technology", "Mechanical Engineering",
            "Civil Engineering", "Electronics & Telecom", "Electrical Engineering", "Applied Sciences"
    });
    private final JComboBox<Integer> yearBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});

    private final JButton registerBtn = new JButton("Submit Registration Request");
    private final JButton backToLoginBtn = new JButton("Already have an account? Log In");
    private final JLabel statusLabel = new JLabel(" ");

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(28, 36, 28, 36)
        ));
        card.setPreferredSize(new Dimension(500, 620));

        JLabel titleLabel = new JLabel("STUDENT REGISTRATION");
        titleLabel.setFont(ModernTheme.FONT_HEADER);
        titleLabel.setForeground(ModernTheme.ACCENT_ORANGE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(titleLabel);

        JLabel subLabel = new JLabel("Account will be verified by Campus Administrator");
        subLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        subLabel.setForeground(ModernTheme.TEXT_MUTED);
        subLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(subLabel);
        card.add(Box.createVerticalStrut(20));

        // Form Fields
        JPanel grid = new JPanel(new GridLayout(6, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(createLabel("Student Roll / ID No: *"));
        ModernTheme.styleTextField(rollField);
        grid.add(rollField);

        grid.add(createLabel("Full Name: *"));
        ModernTheme.styleTextField(nameField);
        grid.add(nameField);

        grid.add(createLabel("College Email: *"));
        ModernTheme.styleTextField(emailField);
        grid.add(emailField);

        grid.add(createLabel("Password (min 6 chars): *"));
        ModernTheme.styleTextField(passField);
        grid.add(passField);

        grid.add(createLabel("Contact Phone:"));
        ModernTheme.styleTextField(phoneField);
        grid.add(phoneField);

        grid.add(createLabel("Academic Department:"));
        ModernTheme.styleComboBox(deptBox);
        grid.add(deptBox);

        card.add(grid);
        card.add(Box.createVerticalStrut(10));

        JPanel yearRow = new JPanel(new GridLayout(1, 2, 12, 0));
        yearRow.setOpaque(false);
        yearRow.add(createLabel("Current Year of Study:"));
        ModernTheme.styleComboBox(yearBox);
        yearRow.add(yearBox);
        card.add(yearRow);

        card.add(Box.createVerticalStrut(18));

        ModernTheme.stylePrimaryButton(registerBtn);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> performRegister());
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(8));

        ModernTheme.styleSecondaryButton(backToLoginBtn);
        backToLoginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backToLoginBtn.setAlignmentX(CENTER_ALIGNMENT);
        backToLoginBtn.addActionListener(e -> mainFrame.showScreen("LOGIN"));
        card.add(backToLoginBtn);
        card.add(Box.createVerticalStrut(8));

        statusLabel.setFont(ModernTheme.FONT_SMALL);
        statusLabel.setForeground(ModernTheme.BADGE_RED);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(statusLabel);

        add(card, new GridBagConstraints());
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ModernTheme.FONT_BODY_BOLD);
        lbl.setForeground(ModernTheme.TEXT_PRIMARY);
        return lbl;
    }

    private void performRegister() {
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String dept = (String) deptBox.getSelectedItem();
        int year = (Integer) yearBox.getSelectedItem();

        if (roll.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all required fields (*).");
            return;
        }

        if (pass.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters.");
            return;
        }

        registerBtn.setEnabled(false);
        statusLabel.setForeground(ModernTheme.BADGE_BLUE);
        statusLabel.setText("Submitting registration request to server...");

        SwingWorkerHelper.execute(() -> AuthService.register(roll, name, email, pass, phone, dept, year), user -> {
            registerBtn.setEnabled(true);
            statusLabel.setText("");
            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYour account is now pending administrator approval.\n" +
                            "You can check verification status from your dashboard.",
                    "Registration Received", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.onUserLoggedIn(user);
        }, error -> {
            registerBtn.setEnabled(true);
            statusLabel.setForeground(ModernTheme.BADGE_RED);
            statusLabel.setText(error.getMessage());
        });
    }
}
