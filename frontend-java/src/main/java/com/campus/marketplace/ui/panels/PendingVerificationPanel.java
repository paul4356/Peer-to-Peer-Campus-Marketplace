package com.campus.marketplace.ui.panels;

import com.campus.marketplace.config.AppConfig;
import com.campus.marketplace.models.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PendingVerificationPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JLabel statusTitle = new JLabel("VERIFICATION PENDING");
    private final JLabel statusDescription = new JLabel("<html><center>Your student account is currently pending verification by the campus administrator.<br>Once approved, you will be able to create listings, message sellers, and participate in the marketplace.</center></html>");
    private final JLabel reasonLabel = new JLabel(" ");
    private final JButton refreshBtn = new JButton("🔄 Check Verification Status");
    private final JButton browseOnlyBtn = new JButton("Browse Marketplace (Read Only)");
    private final JButton logoutBtn = new JButton("Log Out");

    public PendingVerificationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(36, 40, 36, 40)
        ));
        card.setPreferredSize(new Dimension(500, 420));

        JLabel iconLabel = new JLabel("⏳");
        iconLabel.setFont(ModernTheme.FONT_HEADER);
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));

        statusTitle.setFont(ModernTheme.FONT_HEADER);
        statusTitle.setForeground(ModernTheme.BADGE_YELLOW);
        statusTitle.setAlignmentX(CENTER_ALIGNMENT);
        card.add(statusTitle);
        card.add(Box.createVerticalStrut(12));

        statusDescription.setFont(ModernTheme.FONT_BODY);
        statusDescription.setForeground(ModernTheme.TEXT_MUTED);
        statusDescription.setAlignmentX(CENTER_ALIGNMENT);
        card.add(statusDescription);
        card.add(Box.createVerticalStrut(16));

        reasonLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        reasonLabel.setForeground(ModernTheme.BADGE_RED);
        reasonLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(reasonLabel);
        card.add(Box.createVerticalStrut(24));

        ModernTheme.stylePrimaryButton(refreshBtn);
        refreshBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        refreshBtn.setAlignmentX(CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> checkStatus());
        card.add(refreshBtn);
        card.add(Box.createVerticalStrut(10));

        ModernTheme.styleSecondaryButton(browseOnlyBtn);
        browseOnlyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        browseOnlyBtn.setAlignmentX(CENTER_ALIGNMENT);
        browseOnlyBtn.addActionListener(e -> mainFrame.showScreen("MARKETPLACE"));
        card.add(browseOnlyBtn);
        card.add(Box.createVerticalStrut(10));

        ModernTheme.styleSecondaryButton(logoutBtn);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setAlignmentX(CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            AuthService.logout();
            mainFrame.showScreen("LOGIN");
        });
        card.add(logoutBtn);

        add(card, new GridBagConstraints());
    }

    public void updateState(User user) {
        if (user == null) return;

        if (user.isRejected()) {
            statusTitle.setText("VERIFICATION REJECTED");
            statusTitle.setForeground(ModernTheme.BADGE_RED);
            statusDescription.setText("<html><center>Your verification request was declined by the administrator.</center></html>");
            if (user.getRejectionReason() != null && !user.getRejectionReason().isEmpty()) {
                reasonLabel.setText("Reason: " + user.getRejectionReason());
            } else {
                reasonLabel.setText("Please contact the campus student affairs office.");
            }
        } else {
            statusTitle.setText("VERIFICATION PENDING");
            statusTitle.setForeground(ModernTheme.BADGE_YELLOW);
            statusDescription.setText("<html><center>Your student account (" + user.getStudentId() + ") is pending administrator review.<br>Once approved, full marketplace features will be unlocked.</center></html>");
            reasonLabel.setText("Registered Department: " + user.getDepartment());
        }
    }

    private void checkStatus() {
        refreshBtn.setEnabled(false);
        refreshBtn.setText("Checking with server...");

        SwingWorkerHelper.execute(AuthService::fetchCurrentUser, user -> {
            refreshBtn.setEnabled(true);
            refreshBtn.setText("🔄 Check Verification Status");
            updateState(user);

            if (user.isApproved()) {
                JOptionPane.showMessageDialog(this,
                        "Congratulations! Your student account has been approved by the administrator.",
                        "Account Verified", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.onUserLoggedIn(user);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Account status: " + user.getVerificationStatus().toUpperCase(),
                        "Status Update", JOptionPane.INFORMATION_MESSAGE);
            }
        }, error -> {
            refreshBtn.setEnabled(true);
            refreshBtn.setText("🔄 Check Verification Status");
            JOptionPane.showMessageDialog(this, error.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
