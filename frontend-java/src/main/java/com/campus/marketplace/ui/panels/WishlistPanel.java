package com.campus.marketplace.ui.panels;

import com.campus.marketplace.models.Listing;
import com.campus.marketplace.models.WishlistItem;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.ui.dialogs.ProductDetailsDialog;
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

public class WishlistPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;
    private final JLabel headerCount = new JLabel("Saved Wishlist Items");
    private final JButton viewDetailsBtn = new JButton("ℹ️ View Details");
    private final JButton removeBtn = new JButton("🗑️ Remove from Wishlist");
    private final JButton refreshBtn = new JButton("🔄 Refresh");

    private List<WishlistItem> wishlistItems = new ArrayList<>();

    public WishlistPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        headerCount.setFont(ModernTheme.FONT_HEADER);
        headerCount.setForeground(Color.WHITE);
        topBar.add(headerCount, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        ModernTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadData());
        rightActions.add(refreshBtn);
        topBar.add(rightActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        String[] columns = {"Listing ID", "Item Title", "Category", "Price (₹)", "Condition", "Seller Name", "Seller Email", "Saved On"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
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
        viewDetailsBtn.addActionListener(e -> viewDetails());
        bottomBar.add(viewDetailsBtn);

        ModernTheme.styleSecondaryButton(removeBtn);
        removeBtn.setForeground(ModernTheme.BADGE_RED);
        removeBtn.addActionListener(e -> removeItem());
        bottomBar.add(removeBtn);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingWorkerHelper.execute(MarketplaceService::getWishlist, items -> {
            this.wishlistItems = items;
            headerCount.setText("Saved Wishlist Items (" + items.size() + ")");
            tableModel.setRowCount(0);
            for (WishlistItem item : items) {
                tableModel.addRow(new Object[]{
                        item.getId(),
                        item.getTitle(),
                        item.getCategoryName(),
                        "₹" + (int)item.getPrice(),
                        item.getConditionState(),
                        item.getSellerName(),
                        item.getSellerEmail(),
                        item.getSavedAt()
                });
            }
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= wishlistItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        WishlistItem item = wishlistItems.get(row);
        SwingWorkerHelper.execute(() -> MarketplaceService.getListing(item.getId()),
                listing -> {
                    ProductDetailsDialog dialog = new ProductDetailsDialog(mainFrame, listing, (l) -> {});
                    dialog.setVisible(true);
                },
                error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= wishlistItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        WishlistItem item = wishlistItems.get(row);
        SwingWorkerHelper.execute(() -> {
            MarketplaceService.removeFromWishlist(item.getId());
            return null;
        }, res -> {
            JOptionPane.showMessageDialog(this, "Item removed from your wishlist.", "Removed", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }, error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }
}
