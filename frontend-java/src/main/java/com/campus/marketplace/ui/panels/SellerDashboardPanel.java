package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.Listing;
import com.campus.marketplace.models.SellerStats;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class SellerDashboardPanel extends JPanel {
    private final MainFrame mainFrame;

    private final JLabel totalListingsVal = new JLabel("0");
    private final JLabel activeListingsVal = new JLabel("0");
    private final JLabel soldListingsVal = new JLabel("0");
    private final JLabel totalRevenueVal = new JLabel("₹0");
    private final JLabel totalViewsVal = new JLabel("0");

    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;
    private final JButton manageBtn = new JButton("Manage My Listings ➔");
    private final JButton refreshBtn = new JButton("🔄 Refresh Stats");

    public SellerDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("SELLER ANALYTICS & DASHBOARD");
        title.setFont(ModernTheme.FONT_HEADER);
        title.setForeground(Color.WHITE);
        topBar.add(title, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        ModernTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadData());
        rightActions.add(refreshBtn);

        ModernTheme.stylePrimaryButton(manageBtn);
        manageBtn.addActionListener(e -> mainFrame.showScreen("MY_LISTINGS"));
        rightActions.add(manageBtn);

        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ModernTheme.BG_DARK);
        mainContent.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 5 KPI Metric Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 5, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        kpiRow.add(createKpiCard("TOTAL LISTINGS", totalListingsVal, Color.WHITE));
        kpiRow.add(createKpiCard("ACTIVE FOR SALE", activeListingsVal, ModernTheme.BADGE_GREEN));
        kpiRow.add(createKpiCard("ITEMS SOLD", soldListingsVal, ModernTheme.BADGE_BLUE));
        kpiRow.add(createKpiCard("TOTAL REVENUE", totalRevenueVal, ModernTheme.ACCENT_ORANGE));
        kpiRow.add(createKpiCard("PRODUCT VIEWS", totalViewsVal, ModernTheme.BADGE_YELLOW));

        mainContent.add(kpiRow);
        mainContent.add(Box.createVerticalStrut(20));

        // Recent Listings Table
        JLabel tableHeader = new JLabel("RECENT SELLER LISTINGS");
        tableHeader.setFont(ModernTheme.FONT_TITLE);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setAlignmentX(LEFT_ALIGNMENT);
        mainContent.add(tableHeader);
        mainContent.add(Box.createVerticalStrut(10));

        String[] columns = {"ID", "Item Title", "Category", "Price (₹)", "Condition", "Status", "Views", "Date Posted"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(tableModel);
        ModernTheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER, 1));
        scrollPane.setBackground(ModernTheme.SURFACE);
        scrollPane.getViewport().setBackground(ModernTheme.SURFACE);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        mainContent.add(scrollPane);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valLabel, Color valColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ModernTheme.FONT_SMALL_BOLD);
        titleLbl.setForeground(ModernTheme.TEXT_MUTED);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));

        valLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        valLabel.setForeground(valColor);
        card.add(valLabel);

        return card;
    }

    public void loadData() {
        SwingWorkerHelper.execute(MarketplaceService::getSellerDashboard, stats -> {
            totalListingsVal.setText(String.valueOf(stats.getTotalListings()));
            activeListingsVal.setText(String.valueOf(stats.getActiveListings()));
            soldListingsVal.setText(String.valueOf(stats.getSoldListings()));
            totalRevenueVal.setText("₹" + (int)stats.getTotalRevenue());
            totalViewsVal.setText(String.valueOf(stats.getTotalViews()));
        }, null);

        SwingWorkerHelper.execute(MarketplaceService::getMyListings, listings -> {
            tableModel.setRowCount(0);
            for (Listing l : listings) {
                tableModel.addRow(new Object[]{
                        l.getId(),
                        l.getTitle(),
                        l.getCategoryName(),
                        "₹" + (int)l.getPrice(),
                        l.getConditionState(),
                        l.getStatus().toUpperCase(),
                        l.getViewsCount(),
                        l.getCreatedAt()
                });
            }
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }
}
