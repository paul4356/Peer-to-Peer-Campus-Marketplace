package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.Report;
import com.campus.marketplace.service.AdminService;
import com.campus.marketplace.service.MarketplaceService;
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

public class ListingModerationPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;
    private final JLabel headerTitle = new JLabel("Reported Marketplace Listings");

    private final JButton deleteListingBtn = new JButton("🗑️ Delete Inappropriate Listing");
    private final JButton viewDetailsBtn = new JButton("ℹ️ Review Report Details");
    private final JButton backBtn = new JButton("⬅ Back to Admin Dashboard");
    private final JButton refreshBtn = new JButton("🔄 Refresh");

    private List<Report> reports = new ArrayList<>();

    public ListingModerationPanel(MainFrame mainFrame) {
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

        String[] columns = {"Report ID", "Listing ID", "Item Title", "Reason", "Reporter", "Seller", "Price (₹)", "Status", "Date"};
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

        ModernTheme.stylePrimaryButton(viewDetailsBtn);
        viewDetailsBtn.addActionListener(e -> viewSelectedReport());
        bottomBar.add(viewDetailsBtn);

        ModernTheme.styleSecondaryButton(deleteListingBtn);
        deleteListingBtn.setForeground(ModernTheme.BADGE_RED);
        deleteListingBtn.addActionListener(e -> deleteReportedListing());
        bottomBar.add(deleteListingBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingWorkerHelper.execute(AdminService::getReports, reportList -> {
            this.reports = reportList;
            headerTitle.setText("Reported Marketplace Listings (" + reportList.size() + ")");
            tableModel.setRowCount(0);
            for (Report r : reportList) {
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getListingId(),
                        r.getListingTitle(),
                        r.getReason(),
                        r.getReporterName(),
                        r.getSellerName(),
                        "₹" + (int)r.getListingPrice(),
                        r.getStatus().toUpperCase(),
                        r.getCreatedAt()
                });
            }
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    private Report getSelectedReport() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= reports.size()) {
            JOptionPane.showMessageDialog(this, "Please select a report from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return reports.get(row);
    }

    private void viewSelectedReport() {
        Report r = getSelectedReport();
        if (r == null) return;

        JOptionPane.showMessageDialog(this,
                "REPORT DETAILS:\n" +
                        "Listing: " + r.getListingTitle() + " (ID: " + r.getListingId() + ")\n" +
                        "Report Reason: " + r.getReason() + "\n" +
                        "Details: " + (r.getDetails().isEmpty() ? "No extra details" : r.getDetails()) + "\n\n" +
                        "Reported by: " + r.getReporterName() + " (" + r.getReporterEmail() + ")\n" +
                        "Seller: " + r.getSellerName() + " (" + r.getSellerEmail() + ")\n" +
                        "Status: " + r.getStatus().toUpperCase(),
                "Listing Moderation Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteReportedListing() {
        Report r = getSelectedReport();
        if (r == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Moderation Action:\nAre you sure you want to delete listing \"" + r.getListingTitle() + "\" (ID: " + r.getListingId() + ")?\nThis action will immediately remove the listing from the marketplace.",
                "Confirm Listing Removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorkerHelper.execute(() -> {
                MarketplaceService.deleteListing(r.getListingId());
                return null;
            }, res -> {
                JOptionPane.showMessageDialog(this, "Listing removed from marketplace by administrator.", "Listing Removed", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }, err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        }
    }
}
