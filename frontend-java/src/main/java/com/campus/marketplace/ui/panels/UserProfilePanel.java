package com.campus.marketplace.ui.panels;

import com.campus.marketplace.config.AppConfig;
import com.campus.marketplace.models.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

public class UserProfilePanel extends JPanel {
    private final MainFrame mainFrame;

    private final JLabel studentIdVal = new JLabel("---");
    private final JLabel emailVal = new JLabel("---");
    private final JLabel statusBadge = new JLabel("PENDING");

    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<String> deptBox = new JComboBox<>(new String[]{
            "Computer Engineering", "Information Technology", "Mechanical Engineering",
            "Civil Engineering", "Electronics & Telecom", "Electrical Engineering", "Applied Sciences", "Campus Administration"
    });
    private final JComboBox<Integer> yearBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 0});
    private final JPasswordField newPassField = new JPasswordField();

    private final JButton saveBtn = new JButton("Update Profile");

    public UserProfilePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(32, 40, 32, 40)
        ));
        card.setPreferredSize(new Dimension(520, 600));

        JLabel titleLbl = new JLabel("STUDENT PROFILE & SETTINGS");
        titleLbl.setFont(ModernTheme.FONT_HEADER);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(16));

        // Read-only meta row
        JPanel metaRow = new JPanel(new GridLayout(1, 3, 10, 0));
        metaRow.setBackground(ModernTheme.CARD_BG);
        metaRow.setBorder(new EmptyBorder(10, 12, 10, 12));

        metaRow.add(createReadonlyBox("STUDENT ID", studentIdVal));
        metaRow.add(createReadonlyBox("EMAIL", emailVal));
        metaRow.add(createReadonlyBox("VERIFICATION", statusBadge));

        card.add(metaRow);
        card.add(Box.createVerticalStrut(20));

        // Editable form grid
        JPanel grid = new JPanel(new GridLayout(5, 2, 12, 12));
        grid.setOpaque(false);

        grid.add(createLabel("Full Name:"));
        ModernTheme.styleTextField(nameField);
        grid.add(nameField);

        grid.add(createLabel("Phone Number:"));
        ModernTheme.styleTextField(phoneField);
        grid.add(phoneField);

        grid.add(createLabel("Department:"));
        ModernTheme.styleComboBox(deptBox);
        grid.add(deptBox);

        grid.add(createLabel("Year of Study:"));
        ModernTheme.styleComboBox(yearBox);
        grid.add(yearBox);

        grid.add(createLabel("Change Password (optional):"));
        ModernTheme.styleTextField(newPassField);
        grid.add(newPassField);

        card.add(grid);
        card.add(Box.createVerticalStrut(24));

        ModernTheme.stylePrimaryButton(saveBtn);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        saveBtn.setAlignmentX(CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> updateProfile());
        card.add(saveBtn);

        add(card, new GridBagConstraints());
    }

    private JPanel createReadonlyBox(String title, JLabel val) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(ModernTheme.FONT_SMALL_BOLD);
        t.setForeground(ModernTheme.TEXT_MUTED);
        p.add(t);

        val.setFont(ModernTheme.FONT_BODY_BOLD);
        val.setForeground(ModernTheme.ACCENT_ORANGE);
        p.add(val);
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ModernTheme.FONT_BODY_BOLD);
        l.setForeground(ModernTheme.TEXT_PRIMARY);
        return l;
    }

    public void loadProfile() {
        User u = AppConfig.getCurrentUser();
        if (u == null) return;

        studentIdVal.setText(u.getStudentId());
        emailVal.setText(u.getEmail());

        statusBadge.setText(u.getVerificationStatus().toUpperCase());
        if (u.isApproved()) {
            statusBadge.setForeground(ModernTheme.BADGE_GREEN);
        } else if (u.isPending()) {
            statusBadge.setForeground(ModernTheme.BADGE_YELLOW);
        } else {
            statusBadge.setForeground(ModernTheme.BADGE_RED);
        }

        nameField.setText(u.getFullName());
        phoneField.setText(u.getPhone());
        deptBox.setSelectedItem(u.getDepartment());
        yearBox.setSelectedItem(u.getYearOfStudy());
        newPassField.setText("");
    }

    private void updateProfile() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dept = (String) deptBox.getSelectedItem();
        int year = (Integer) yearBox.getSelectedItem();
        String pass = new String(newPassField.getPassword()).trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        saveBtn.setEnabled(false);
        saveBtn.setText("Saving...");

        SwingWorkerHelper.execute(() -> AuthService.updateProfile(name, phone, dept, year, pass), updatedUser -> {
            saveBtn.setEnabled(true);
            saveBtn.setText("Update Profile");
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.updateUserHeader();
        }, error -> {
            saveBtn.setEnabled(true);
            saveBtn.setText("Update Profile");
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
