package com.campus.marketplace.ui;

import com.campus.marketplace.config.AppConfig;
import com.campus.marketplace.models.User;
import com.campus.marketplace.service.AuthService;
import com.campus.marketplace.ui.panels.AdminDashboardPanel;
import com.campus.marketplace.ui.panels.ListingModerationPanel;
import com.campus.marketplace.ui.panels.LoginPanel;
import com.campus.marketplace.ui.panels.MarketplacePanel;
import com.campus.marketplace.ui.panels.MyListingsPanel;
import com.campus.marketplace.ui.panels.PendingVerificationPanel;
import com.campus.marketplace.ui.panels.RegisterPanel;
import com.campus.marketplace.ui.panels.SellerDashboardPanel;
import com.campus.marketplace.ui.panels.StudentVerificationPanel;
import com.campus.marketplace.ui.panels.UserProfilePanel;
import com.campus.marketplace.ui.panels.WishlistPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsContainer = new JPanel(cardLayout);

    // Nav bar components
    private final JPanel navBar = new JPanel(new BorderLayout());
    private final JPanel navButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

    private final JButton navMarketplace = new JButton("Marketplace");
    private final JButton navWishlist = new JButton("Wishlist");
    private final JButton navMyListings = new JButton("My Listings");
    private final JButton navSellerDashboard = new JButton("Seller Dashboard");
    private final JButton navAdmin = new JButton("🛡️ Admin Console");

    private final JLabel userBadge = new JLabel("Guest");
    private final JButton profileBtn = new JButton("Profile");
    private final JButton logoutBtn = new JButton("Log Out");

    // Panels
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private final PendingVerificationPanel pendingPanel;
    private final MarketplacePanel marketplacePanel;
    private final MyListingsPanel myListingsPanel;
    private final WishlistPanel wishlistPanel;
    private final SellerDashboardPanel sellerDashboardPanel;
    private final UserProfilePanel userProfilePanel;
    private final AdminDashboardPanel adminDashboardPanel;
    private final StudentVerificationPanel studentVerificationPanel;
    private final ListingModerationPanel listingModerationPanel;

    // Status bar
    private final JLabel apiStatusLabel = new JLabel("API: " + AppConfig.getApiBaseUrl() + " | Ready");

    public MainFrame() {
        super(AppConfig.APP_TITLE + " (" + AppConfig.APP_VERSION + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout());

        // Initialize panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        pendingPanel = new PendingVerificationPanel(this);
        marketplacePanel = new MarketplacePanel(this);
        myListingsPanel = new MyListingsPanel(this);
        wishlistPanel = new WishlistPanel(this);
        sellerDashboardPanel = new SellerDashboardPanel(this);
        userProfilePanel = new UserProfilePanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        studentVerificationPanel = new StudentVerificationPanel(this);
        listingModerationPanel = new ListingModerationPanel(this);

        // Add to card layout container
        cardsContainer.add(loginPanel, "LOGIN");
        cardsContainer.add(registerPanel, "REGISTER");
        cardsContainer.add(pendingPanel, "PENDING_VERIFICATION");
        cardsContainer.add(marketplacePanel, "MARKETPLACE");
        cardsContainer.add(myListingsPanel, "MY_LISTINGS");
        cardsContainer.add(wishlistPanel, "WISHLIST");
        cardsContainer.add(sellerDashboardPanel, "SELLER_DASHBOARD");
        cardsContainer.add(userProfilePanel, "PROFILE");
        cardsContainer.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        cardsContainer.add(studentVerificationPanel, "ADMIN_VERIFICATIONS");
        cardsContainer.add(listingModerationPanel, "ADMIN_MODERATION");

        // Construct Navigation Header
        buildNavBar();
        add(navBar, BorderLayout.NORTH);
        add(cardsContainer, BorderLayout.CENTER);

        // Construct Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(ModernTheme.SURFACE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER),
                new EmptyBorder(6, 16, 6, 16)
        ));

        apiStatusLabel.setFont(ModernTheme.FONT_SMALL);
        apiStatusLabel.setForeground(ModernTheme.TEXT_MUTED);
        statusBar.add(apiStatusLabel, BorderLayout.WEST);

        JLabel techBadge = new JLabel("REST API Client (PHP 8.3 + MySQL)");
        techBadge.setFont(ModernTheme.FONT_SMALL);
        techBadge.setForeground(ModernTheme.TEXT_DIM);
        statusBar.add(techBadge, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);

        // Start at Login screen
        showScreen("LOGIN");
        updateUserHeader();
    }

    private void buildNavBar() {
        navBar.setBackground(ModernTheme.SURFACE);
        navBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        // Brand Title
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandBox.setOpaque(false);

        JLabel brand = new JLabel("STUDY ESSENTIALS");
        brand.setFont(ModernTheme.FONT_HEADER);
        brand.setForeground(ModernTheme.ACCENT_ORANGE);
        brandBox.add(brand);

        // Navigation buttons
        navButtonsPanel.setOpaque(false);
        styleNavButton(navMarketplace, "MARKETPLACE");
        styleNavButton(navWishlist, "WISHLIST");
        styleNavButton(navMyListings, "MY_LISTINGS");
        styleNavButton(navSellerDashboard, "SELLER_DASHBOARD");
        styleNavButton(navAdmin, "ADMIN_DASHBOARD");

        navButtonsPanel.add(navMarketplace);
        navButtonsPanel.add(navWishlist);
        navButtonsPanel.add(navMyListings);
        navButtonsPanel.add(navSellerDashboard);
        navButtonsPanel.add(navAdmin);

        brandBox.add(navButtonsPanel);
        navBar.add(brandBox, BorderLayout.WEST);

        // User info chip and Logout
        userSection.setOpaque(false);

        userBadge.setFont(ModernTheme.FONT_BODY_BOLD);
        userBadge.setForeground(Color.WHITE);
        userSection.add(userBadge);

        ModernTheme.styleSecondaryButton(profileBtn);
        profileBtn.addActionListener(e -> showScreen("PROFILE"));
        userSection.add(profileBtn);

        ModernTheme.styleSecondaryButton(logoutBtn);
        logoutBtn.addActionListener(e -> {
            AuthService.logout();
            showScreen("LOGIN");
            updateUserHeader();
        });
        userSection.add(logoutBtn);

        navBar.add(userSection, BorderLayout.EAST);
    }

    private void styleNavButton(JButton btn, String targetScreen) {
        btn.setFont(ModernTheme.FONT_BODY_BOLD);
        btn.setForeground(ModernTheme.TEXT_MUTED);
        btn.setBackground(ModernTheme.SURFACE);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showScreen(targetScreen));
    }

    public void showScreen(String screenName) {
        cardLayout.show(cardsContainer, screenName);

        // Refresh data when switching to appropriate screens
        switch (screenName) {
            case "MARKETPLACE" -> marketplacePanel.initData();
            case "MY_LISTINGS" -> myListingsPanel.loadData();
            case "WISHLIST" -> wishlistPanel.loadData();
            case "SELLER_DASHBOARD" -> sellerDashboardPanel.loadData();
            case "PROFILE" -> userProfilePanel.loadProfile();
            case "ADMIN_DASHBOARD" -> adminDashboardPanel.loadData();
            case "ADMIN_VERIFICATIONS" -> studentVerificationPanel.loadData();
            case "ADMIN_MODERATION" -> listingModerationPanel.loadData();
            case "PENDING_VERIFICATION" -> pendingPanel.updateState(AppConfig.getCurrentUser());
        }

        // Highlight active nav button
        updateNavHighlight(screenName);
    }

    private void updateNavHighlight(String activeScreen) {
        navMarketplace.setForeground("MARKETPLACE".equals(activeScreen) ? ModernTheme.ACCENT_ORANGE : ModernTheme.TEXT_MUTED);
        navWishlist.setForeground("WISHLIST".equals(activeScreen) ? ModernTheme.ACCENT_ORANGE : ModernTheme.TEXT_MUTED);
        navMyListings.setForeground("MY_LISTINGS".equals(activeScreen) ? ModernTheme.ACCENT_ORANGE : ModernTheme.TEXT_MUTED);
        navSellerDashboard.setForeground("SELLER_DASHBOARD".equals(activeScreen) ? ModernTheme.ACCENT_ORANGE : ModernTheme.TEXT_MUTED);
        navAdmin.setForeground(activeScreen.startsWith("ADMIN") ? ModernTheme.ACCENT_ORANGE : ModernTheme.TEXT_MUTED);
    }

    public void onUserLoggedIn(User user) {
        updateUserHeader();

        if (user.isAdmin()) {
            showScreen("ADMIN_DASHBOARD");
        } else if (user.isApproved()) {
            showScreen("MARKETPLACE");
        } else {
            showScreen("PENDING_VERIFICATION");
        }
    }

    public void updateUserHeader() {
        boolean loggedIn = AppConfig.isLoggedIn();
        User user = AppConfig.getCurrentUser();

        navBar.setVisible(loggedIn);

        if (loggedIn && user != null) {
            String rolePill = user.isAdmin() ? "[ADMIN]" :
                    (user.isApproved() ? "[VERIFIED STUDENT]" : "[" + user.getVerificationStatus().toUpperCase() + "]");
            userBadge.setText("👤 " + user.getFullName() + " " + rolePill);
            navAdmin.setVisible(user.isAdmin());
            profileBtn.setVisible(true);
            logoutBtn.setVisible(true);
        } else {
            userBadge.setText("Guest");
            navAdmin.setVisible(false);
            profileBtn.setVisible(false);
            logoutBtn.setVisible(false);
        }
    }
}
