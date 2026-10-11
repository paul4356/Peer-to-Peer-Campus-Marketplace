package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.AdminMetrics;
import com.campus.marketplace.service.AdminService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AdminDashboardPanel extends JPanel {
    private final MainFrame mainFrame;

    private final JLabel totalUsersVal = new JLabel("0");
    private final JLabel approvedStudentsVal = new JLabel("0");
    private final JLabel pendingVerificationsVal = new JLabel("0");
    private final JLabel totalListingsVal = new JLabel("0");
    private final JLabel totalReportsVal = new JLabel("0");
    private final JLabel totalViewsVal = new JLabel("0");

    private final JButton verificationsBtn = new JButton("Manage Student Verifications ➔");
    private final JButton moderationBtn = new JButton("Listing Moderation Queue ➔");
    private final JButton refreshBtn = new JButton("🔄 Refresh Data");

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("CAMPUS ADMINISTRATOR CONSOLE");
        title.setFont(ModernTheme.FONT_HEADER);
        title.setForeground(ModernTheme.ACCENT_ORANGE);
        topBar.add(title, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        ModernTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadData());
        rightActions.add(refreshBtn);
        topBar.add(rightActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ModernTheme.BG_DARK);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 6 KPI Metric Cards
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        grid.add(createCard("TOTAL REGISTERED USERS", totalUsersVal, Color.WHITE));
        grid.add(createCard("VERIFIED APPROVED STUDENTS", approvedStudentsVal, ModernTheme.BADGE_GREEN));
        grid.add(createCard("PENDING VERIFICATIONS", pendingVerificationsVal, ModernTheme.BADGE_YELLOW));
        grid.add(createCard("TOTAL MARKETPLACE LISTINGS", totalListingsVal, ModernTheme.BADGE_BLUE));
        grid.add(createCard("REPORTED LISTINGS (MODERATION)", totalReportsVal, ModernTheme.BADGE_RED));
        grid.add(createCard("CUMULATIVE PRODUCT VIEWS", totalViewsVal, ModernTheme.TEXT_MUTED));

        content.add(grid);
        content.add(Box.createVerticalStrut(28));

        // Quick Navigation Buttons Box
        JLabel navHeader = new JLabel("ADMINISTRATIVE ACTIONS");
        navHeader.setFont(ModernTheme.FONT_TITLE);
        navHeader.setForeground(Color.WHITE);
        navHeader.setAlignmentX(LEFT_ALIGNMENT);
        content.add(navHeader);
        content.add(Box.createVerticalStrut(12));

        JPanel navRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navRow.setOpaque(false);
        navRow.setAlignmentX(LEFT_ALIGNMENT);

        ModernTheme.stylePrimaryButton(verificationsBtn);
        verificationsBtn.addActionListener(e -> mainFrame.showScreen("ADMIN_VERIFICATIONS"));
        navRow.add(verificationsBtn);

        ModernTheme.styleSecondaryButton(moderationBtn);
        moderationBtn.addActionListener(e -> mainFrame.showScreen("ADMIN_MODERATION"));
        navRow.add(moderationBtn);

        content.add(navRow);
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel val, Color valColor) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(ModernTheme.SURFACE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel t = new JLabel(title);
        t.setFont(ModernTheme.FONT_SMALL_BOLD);
        t.setForeground(ModernTheme.TEXT_MUTED);
        c.add(t);
        c.add(Box.createVerticalStrut(10));

        val.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        val.setForeground(valColor);
        c.add(val);

        return c;
    }

    public void loadData() {
        SwingWorkerHelper.execute(AdminService::getDashboardMetrics, metrics -> {
            totalUsersVal.setText(String.valueOf(metrics.getTotalUsers()));
            approvedStudentsVal.setText(String.valueOf(metrics.getApprovedStudents()));
            pendingVerificationsVal.setText(String.valueOf(metrics.getPendingVerifications()));
            totalListingsVal.setText(String.valueOf(metrics.getTotalListings()));
            totalReportsVal.setText(String.valueOf(metrics.getPendingReports()));
            totalViewsVal.setText(String.valueOf(metrics.getTotalViews()));
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Admin Metrics Error", JOptionPane.ERROR_MESSAGE));
    }
}
