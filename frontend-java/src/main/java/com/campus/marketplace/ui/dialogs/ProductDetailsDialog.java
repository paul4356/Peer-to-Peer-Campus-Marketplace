package com.campus.marketplace.ui.dialogs;

import com.campus.marketplace.config.AppConfig;
import com.campus.marketplace.models.Listing;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ProductDetailsDialog extends JDialog {
    private final Listing listing;
    private final Consumer<Listing> onWishlist;

    public ProductDetailsDialog(Frame parent, Listing listing, Consumer<Listing> onWishlist) {
        super(parent, "Product Details: " + listing.getTitle(), true);
        this.listing = listing;
        this.onWishlist = onWishlist;

        setSize(560, 600);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(ModernTheme.SURFACE);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header Title & Price
        JLabel catLabel = new JLabel(listing.getCategoryName().toUpperCase());
        catLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        catLabel.setForeground(ModernTheme.ACCENT_ORANGE);
        root.add(catLabel);
        root.add(Box.createVerticalStrut(4));

        JLabel titleLabel = new JLabel("<html><b>" + listing.getTitle() + "</b></html>");
        titleLabel.setFont(ModernTheme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        root.add(titleLabel);
        root.add(Box.createVerticalStrut(10));

        // Meta Stats Bar (Price, Condition, Status, Views)
        JPanel metaBox = new JPanel(new GridLayout(2, 2, 10, 8));
        metaBox.setBackground(ModernTheme.CARD_BG);
        metaBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        metaBox.add(createMetaField("Price:", "₹" + (int) listing.getPrice(), ModernTheme.ACCENT_ORANGE));
        metaBox.add(createMetaField("Condition:", listing.getConditionState().toUpperCase(), ModernTheme.BADGE_GREEN));
        metaBox.add(createMetaField("Availability:", listing.getStatus().toUpperCase(), ModernTheme.BADGE_BLUE));
        metaBox.add(createMetaField("Marketplace Views:", String.valueOf(listing.getViewsCount()), ModernTheme.TEXT_MUTED));

        root.add(metaBox);
        root.add(Box.createVerticalStrut(16));

        // Description Box
        JLabel descHeader = new JLabel("ITEM DESCRIPTION");
        descHeader.setFont(ModernTheme.FONT_BODY_BOLD);
        descHeader.setForeground(Color.WHITE);
        root.add(descHeader);
        root.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea(listing.getDescription());
        descArea.setFont(ModernTheme.FONT_BODY);
        descArea.setForeground(ModernTheme.TEXT_PRIMARY);
        descArea.setBackground(ModernTheme.CARD_BG);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(500, 120));
        descScroll.setBorder(null);
        root.add(descScroll);
        root.add(Box.createVerticalStrut(16));

        // Seller Contact Information
        JLabel sellerHeader = new JLabel("STUDENT SELLER DETAILS");
        sellerHeader.setFont(ModernTheme.FONT_BODY_BOLD);
        sellerHeader.setForeground(Color.WHITE);
        root.add(sellerHeader);
        root.add(Box.createVerticalStrut(6));

        JPanel sellerBox = new JPanel(new GridLayout(2, 2, 8, 6));
        sellerBox.setBackground(ModernTheme.CARD_BG);
        sellerBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        Listing.Seller seller = listing.getSeller();
        sellerBox.add(createMetaField("Seller:", seller != null ? seller.getName() : "Student", Color.WHITE));
        sellerBox.add(createMetaField("Email:", seller != null ? seller.getEmail() : "N/A", ModernTheme.TEXT_MUTED));
        sellerBox.add(createMetaField("Department:", seller != null ? seller.getDepartment() : "General", ModernTheme.TEXT_MUTED));
        sellerBox.add(createMetaField("Phone:", seller != null && seller.getPhone() != null ? seller.getPhone() : "Provided on Chat", ModernTheme.TEXT_MUTED));

        root.add(sellerBox);

        // Footer Action Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setBackground(ModernTheme.SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));

        JButton reportBtn = new JButton("⚠️ Report Item");
        ModernTheme.styleSecondaryButton(reportBtn);
        reportBtn.setForeground(ModernTheme.BADGE_RED);
        reportBtn.addActionListener(e -> reportListing());
        footer.add(reportBtn);

        JButton wishBtn = new JButton("♡ Save to Wishlist");
        ModernTheme.styleSecondaryButton(wishBtn);
        wishBtn.addActionListener(e -> {
            if (onWishlist != null) onWishlist.accept(listing);
        });
        footer.add(wishBtn);

        JButton closeBtn = new JButton("Close");
        ModernTheme.stylePrimaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(new JScrollPane(root), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createMetaField(String title, String val, Color valColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(ModernTheme.FONT_SMALL_BOLD);
        t.setForeground(ModernTheme.TEXT_MUTED);
        p.add(t);

        JLabel v = new JLabel(val);
        v.setFont(ModernTheme.FONT_BODY_BOLD);
        v.setForeground(valColor);
        p.add(v);
        return p;
    }

    private void reportListing() {
        if (!AppConfig.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in to report listings.", "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Enter reason for reporting listing \"" + listing.getTitle() + "\":\n(e.g., Inappropriate item, Prohibited goods, Duplicate, Misleading price)",
                "Report Listing to Admin", JOptionPane.WARNING_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            String details = JOptionPane.showInputDialog(this, "Additional details (optional):", "Report Details", JOptionPane.PLAIN_MESSAGE);
            SwingWorkerHelper.execute(() -> {
                MarketplaceService.reportListing(listing.getId(), reason.trim(), details != null ? details.trim() : "");
                return null;
            }, res -> JOptionPane.showMessageDialog(this,
                    "Thank you. Your report has been submitted for administrator review.",
                    "Report Received", JOptionPane.INFORMATION_MESSAGE),
            err -> JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        }
    }
}
