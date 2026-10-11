package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.User;
import com.campus.marketplace.service.AdminService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StudentVerificationPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;
    private final JLabel headerTitle = new JLabel("Pending Student Account Verifications");

    private final JButton approveBtn = new JButton("✅ Approve Student");
    private final JButton rejectBtn = new JButton("❌ Reject with Reason");
    private final JButton backBtn = new JButton("⬅ Back to Admin Dashboard");
    private final JButton refreshBtn = new JButton("🔄 Refresh");

    private List<User> pendingUsers = new ArrayList<>();

    public StudentVerificationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        headerTitle.setFont(ModernTheme.FONT_HEADER);
        headerTitle.setForeground(Color.WHITE);
        topBar.add(headerTitle, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        ModernTheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> mainFrame.showScreen("ADMIN_DASHBOARD"));
        rightActions.add(backBtn);

        ModernTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadData());
        rightActions.add(refreshBtn);

        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] columns = {"ID", "Student Roll No", "Full Name", "College Email", "Department", "Year", "Phone", "Applied At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernTheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(16, 24, 16, 24));
        scrollPane.setBackground(ModernTheme.BG_DARK);
        scrollPane.getViewport().setBackground(ModernTheme.SURFACE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        bottomBar.setBackground(ModernTheme.SURFACE);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));

        ModernTheme.stylePrimaryButton(approveBtn);
        approveBtn.setBackground(new Color(22, 101, 52));
        approveBtn.addActionListener(e -> approveSelected());
        bottomBar.add(approveBtn);

        ModernTheme.styleSecondaryButton(rejectBtn);
        rejectBtn.setForeground(ModernTheme.BADGE_RED);
        rejectBtn.addActionListener(e -> rejectSelected());
        bottomBar.add(rejectBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingWorkerHelper.execute(AdminService::getPendingVerifications, users -> {
            this.pendingUsers = users;
            headerTitle.setText("Pending Student Account Verifications (" + users.size() + ")");
            tableModel.setRowCount(0);
            for (User u : users) {
                tableModel.addRow(new Object[]{
                        u.getId(),
                        u.getStudentId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getDepartment(),
                        u.getYearOfStudy(),
                        u.getPhone(),
                        "Pending"
                });
            }
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= pendingUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return pendingUsers.get(row);
    }

    private void approveSelected() {
        User u = getSelectedUser();
        if (u == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve verification request for:\n" + u.getFullName() + " (" + u.getStudentId() + ") - " + u.getEmail() + "?\nThis will grant student marketplace privileges.",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorkerHelper.execute(() -> {
                AdminService.updateVerification(u.getId(), "approved", null);
                return null;
            }, res -> {
                JOptionPane.showMessageDialog(this, "Student account verified successfully!", "Approved", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }, err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void rejectSelected() {
        User u = getSelectedUser();
        if (u == null) return;

        String reason = JOptionPane.showInputDialog(this,
                "Enter reason for rejecting verification of " + u.getFullName() + ":\n(e.g., Unrecognized student roll number, Invalid student ID card image)",
                "Reject Verification", JOptionPane.WARNING_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            SwingWorkerHelper.execute(() -> {
                AdminService.updateVerification(u.getId(), "rejected", reason.trim());
                return null;
            }, res -> {
                JOptionPane.showMessageDialog(this, "Student verification rejected. Reason saved.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }, err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        }
    }
}
