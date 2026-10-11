package com.campus.marketplace.ui.components;

import com.campus.marketplace.models.Listing;
import com.campus.marketplace.ui.ModernTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ProductCardPanel extends JPanel {
    private final Listing listing;
    private final Consumer<Listing> onChat;
    private final Consumer<Listing> onDetails;
    private final Consumer<Listing> onWishlistToggle;
    private boolean isHovered = false;

    public ProductCardPanel(Listing listing, Consumer<Listing> onChat, Consumer<Listing> onDetails, Consumer<Listing> onWishlistToggle) {
        this.listing = listing;
        this.onChat = onChat;
        this.onDetails = onDetails;
        this.onWishlistToggle = onWishlistToggle;

        setLayout(new BorderLayout());
        setBackground(ModernTheme.CARD_BG);
        setPreferredSize(new Dimension(270, 330));
        setMaximumSize(new Dimension(320, 330));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(0, 0, 10, 0)
        ));

        // Top Banner / Image Area
        JPanel imageArea = createTopBanner();
        add(imageArea, BorderLayout.NORTH);

        // Body Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 12, 6, 12));

        // Category Tag (Orange)
        JLabel categoryLabel = new JLabel(listing.getCategoryName().toUpperCase());
        categoryLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        categoryLabel.setForeground(ModernTheme.ACCENT_ORANGE);
        contentPanel.add(categoryLabel);
        contentPanel.add(Box.createVerticalStrut(4));

        // Title and Price Row
        JPanel titlePriceRow = new JPanel(new BorderLayout(8, 0));
        titlePriceRow.setOpaque(false);

        String displayTitle = listing.getTitle();
        if (displayTitle.length() > 28) {
            displayTitle = displayTitle.substring(0, 26) + "...";
        }
        JLabel titleLabel = new JLabel("<html><b>" + displayTitle + "</b></html>");
        titleLabel.setFont(ModernTheme.FONT_SUBTITLE);
        titleLabel.setForeground(Color.WHITE);
        titlePriceRow.add(titleLabel, BorderLayout.CENTER);

        JLabel priceLabel = new JLabel("₹" + (int) listing.getPrice());
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(Color.WHITE);
        titlePriceRow.add(priceLabel, BorderLayout.EAST);

        contentPanel.add(titlePriceRow);
        contentPanel.add(Box.createVerticalStrut(4));

        // Seller
        JLabel sellerLabel = new JLabel("BY " + listing.getSellerName().toUpperCase());
        sellerLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        sellerLabel.setForeground(ModernTheme.TEXT_MUTED);
        contentPanel.add(sellerLabel);

        contentPanel.add(Box.createVerticalGlue());

        // Buttons Row
        JPanel actionsRow = new JPanel(new BorderLayout(6, 0));
        actionsRow.setOpaque(false);

        JButton chatBtn = new JButton("💬 CHAT");
        chatBtn.setFont(ModernTheme.FONT_BODY_BOLD);
        chatBtn.setBackground(Color.WHITE);
        chatBtn.setForeground(new Color(18, 18, 20));
        chatBtn.setFocusPainted(false);
        chatBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chatBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        chatBtn.addActionListener(e -> {
            if (onChat != null) onChat.accept(listing);
        });
        actionsRow.add(chatBtn, BorderLayout.CENTER);

        JPanel iconBtnsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        iconBtnsPanel.setOpaque(false);

        JButton infoBtn = new JButton("ℹ️");
        ModernTheme.styleSecondaryButton(infoBtn);
        infoBtn.setPreferredSize(new Dimension(36, 32));
        infoBtn.setToolTipText("View Full Product Details");
        infoBtn.addActionListener(e -> {
            if (onDetails != null) onDetails.accept(listing);
        });
        iconBtnsPanel.add(infoBtn);

        JButton wishBtn = new JButton("♡");
        ModernTheme.styleSecondaryButton(wishBtn);
        wishBtn.setPreferredSize(new Dimension(36, 32));
        wishBtn.setToolTipText("Save to Wishlist");
        wishBtn.addActionListener(e -> {
            if (onWishlistToggle != null) onWishlistToggle.accept(listing);
        });
        iconBtnsPanel.add(wishBtn);

        actionsRow.add(iconBtnsPanel, BorderLayout.EAST);
        contentPanel.add(actionsRow);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(38, 38, 44));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle diagonal grid accent
                g2.setColor(new Color(50, 50, 58));
                for (int x = 0; x < getWidth(); x += 24) {
                    g2.drawLine(x, 0, x + 40, getHeight());
                }

                // Category Icon Symbol
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
                String emoji = getCategoryEmoji(listing.getCategoryName());
                g2.drawString(emoji, (getWidth() / 2) - 22, (getHeight() / 2) + 15);
                g2.dispose();
            }
        };
        banner.setPreferredSize(new Dimension(260, 150));
        banner.setLayout(new BorderLayout());

        // Condition Badge Pill at Top Right
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        badgeWrapper.setOpaque(false);

        JLabel conditionBadge = new JLabel(listing.getConditionState().toUpperCase());
        conditionBadge.setFont(ModernTheme.FONT_SMALL_BOLD);
        conditionBadge.setOpaque(true);
        conditionBadge.setBackground(new Color(18, 18, 20, 220));
        conditionBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getConditionColor(listing.getConditionState()), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        conditionBadge.setForeground(getConditionColor(listing.getConditionState()));

        badgeWrapper.add(conditionBadge);
        banner.add(badgeWrapper, BorderLayout.NORTH);

        return banner;
    }

    private String getCategoryEmoji(String category) {
        if (category == null) return "📦";
        String lower = category.toLowerCase();
        if (lower.contains("book")) return "📚";
        if (lower.contains("calc")) return "🧮";
        if (lower.contains("coat") || lower.contains("access")) return "🥼";
        if (lower.contains("bike") || lower.contains("cycle")) return "🚲";
        if (lower.contains("draw") || lower.contains("stat") || lower.contains("eg")) return "📐";
        if (lower.contains("lab") || lower.contains("equip")) return "🔬";
        if (lower.contains("elect")) return "⚡";
        return "📦";
    }

    private Color getConditionColor(String condition) {
        if (condition == null) return ModernTheme.BADGE_GREEN;
        return switch (condition.toUpperCase()) {
            case "NEW" -> ModernTheme.BADGE_BLUE;
            case "LIKE NEW" -> new Color(129, 140, 248);
            case "GOOD" -> ModernTheme.BADGE_GREEN;
            case "ACCEPTABLE" -> ModernTheme.BADGE_YELLOW;
            default -> ModernTheme.TEXT_MUTED;
        };
    }
}
