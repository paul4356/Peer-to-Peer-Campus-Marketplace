package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.Category;
import com.campus.marketplace.models.Listing;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.ui.dialogs.AddListingDialog;
import com.campus.marketplace.ui.dialogs.EditListingDialog;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

public class MyListingsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;
    private final JLabel headerCount = new JLabel("My Marketplace Listings");
    private final JButton editBtn = new JButton("✏️ Edit Selected");
    private final JButton markSoldBtn = new JButton("🏷️ Mark as Sold");
    private final JButton deleteBtn = new JButton("🗑️ Delete Listing");
    private final JButton postNewBtn = new JButton("+ Post New Item");
    private final JButton refreshBtn = new JButton("🔄 Refresh");

    private List<Listing> myListings = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    public MyListingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        // Header Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        headerCount.setFont(ModernTheme.FONT_HEADER);
        headerCount.setForeground(Color.WHITE);
        topBar.add(headerCount, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        ModernTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadData());
        actionBtns.add(refreshBtn);

        ModernTheme.stylePrimaryButton(postNewBtn);
        postNewBtn.addActionListener(e -> openAddDialog());
        actionBtns.add(postNewBtn);

        topBar.add(actionBtns, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // JTable Configuration
        String[] columns = {"ID", "Title", "Category", "Price (₹)", "Condition", "Status", "Views", "Posted Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernTheme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(16, 24, 16, 24));
        scrollPane.setBackground(ModernTheme.BG_DARK);
        scrollPane.getViewport().setBackground(ModernTheme.SURFACE);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        bottomBar.setBackground(ModernTheme.SURFACE);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));

        ModernTheme.styleSecondaryButton(editBtn);
        editBtn.addActionListener(e -> openEditDialog());
        bottomBar.add(editBtn);

        ModernTheme.styleSecondaryButton(markSoldBtn);
        markSoldBtn.addActionListener(e -> toggleSoldStatus());
        bottomBar.add(markSoldBtn);

        ModernTheme.styleSecondaryButton(deleteBtn);
        deleteBtn.setForeground(ModernTheme.BADGE_RED);
        deleteBtn.addActionListener(e -> deleteSelectedListing());
        bottomBar.add(deleteBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingWorkerHelper.execute(() -> {
            categories = MarketplaceService.getCategories();
            return MarketplaceService.getMyListings();
        }, listings -> {
            this.myListings = listings;
            headerCount.setText("My Marketplace Listings (" + listings.size() + ")");
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

    private Listing getSelectedListing() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= myListings.size()) {
            JOptionPane.showMessageDialog(this, "Please select a listing from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return myListings.get(row);
    }

    private void openAddDialog() {
        AddListingDialog dialog = new AddListingDialog(mainFrame, categories, this::loadData);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        Listing selected = getSelectedListing();
        if (selected == null) return;
        EditListingDialog dialog = new EditListingDialog(mainFrame, selected, categories, this::loadData);
        dialog.setVisible(true);
    }

    private void toggleSoldStatus() {
        Listing selected = getSelectedListing();
        if (selected == null) return;

        String newStatus = "sold".equalsIgnoreCase(selected.getStatus()) ? "available" : "sold";
        SwingWorkerHelper.execute(() -> {
            MarketplaceService.updateListingStatus(selected.getId(), newStatus);
            return null;
        }, res -> {
            JOptionPane.showMessageDialog(this, "Listing status updated to " + newStatus.toUpperCase(), "Status Changed", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void deleteSelectedListing() {
        Listing selected = getSelectedListing();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete listing \"" + selected.getTitle() + "\"?\nThis destructive action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorkerHelper.execute(() -> {
                MarketplaceService.deleteListing(selected.getId());
                return null;
            }, res -> {
                JOptionPane.showMessageDialog(this, "Listing deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        }
    }
}
