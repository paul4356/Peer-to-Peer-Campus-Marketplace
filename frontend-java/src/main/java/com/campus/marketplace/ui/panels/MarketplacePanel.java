package com.campus.marketplace.ui.panels;

import com.campus.marketplace.config.AppConfig;
import com.campus.marketplace.models.Category;
import com.campus.marketplace.models.Listing;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.MainFrame;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.ui.components.ProductCardPanel;
import com.campus.marketplace.ui.dialogs.AddListingDialog;
import com.campus.marketplace.ui.dialogs.ProductDetailsDialog;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MarketplacePanel extends JPanel {
    private final MainFrame mainFrame;

    private final JTextField searchField = new JTextField(25);
    private final JButton searchBtn = new JButton("Search");
    private final JButton clearBtn = new JButton("✕");
    private final JComboBox<String> sortCombo = new JComboBox<>(new String[]{
            "Newest First", "Price: Low to High", "Price: High to Low", "Most Viewed"
    });
    private final JButton addListingBtn = new JButton("+ Post Listing");
    private final JButton refreshBtn = new JButton("🔄 Refresh");

    private final JPanel cardsGrid = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane();
    private final JLabel statusLabel = new JLabel("Loading campus catalog...");
    private final JLabel resultsCountLabel = new JLabel("Showing 0 items");

    private final JPanel categorySidebar = new JPanel();
    private final ButtonGroup categoryGroup = new ButtonGroup();
    private int selectedCategoryId = 0; // 0 = ALL
    private String selectedCondition = "ALL";

    private List<Listing> currentListings = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    public MarketplacePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        // 1. Top Controls Bar
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // 2. Left Sidebar (Category & Condition Filters)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 3. Center Cards Container
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(ModernTheme.BG_DARK);
        centerContainer.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel statsBar = new JPanel(new BorderLayout());
        statsBar.setOpaque(false);
        statsBar.setBorder(new EmptyBorder(0, 4, 8, 4));

        resultsCountLabel.setFont(ModernTheme.FONT_BODY_BOLD);
        resultsCountLabel.setForeground(ModernTheme.TEXT_MUTED);
        statsBar.add(resultsCountLabel, BorderLayout.WEST);

        statusLabel.setFont(ModernTheme.FONT_SMALL);
        statusLabel.setForeground(ModernTheme.ACCENT_ORANGE);
        statsBar.add(statusLabel, BorderLayout.EAST);

        centerContainer.add(statsBar, BorderLayout.NORTH);

        cardsGrid.setBackground(ModernTheme.BG_DARK);
        cardsGrid.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));

        scrollPane.setViewportView(cardsGrid);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ModernTheme.BG_DARK);
        scrollPane.getViewport().setBackground(ModernTheme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerContainer.add(scrollPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(ModernTheme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Left: Search input with clear button
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(ModernTheme.FONT_TITLE);
        searchBox.add(searchIcon);

        ModernTheme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(320, 36));
        searchField.addActionListener(e -> loadListings());
        searchBox.add(searchField);

        ModernTheme.styleSecondaryButton(clearBtn);
        clearBtn.setPreferredSize(new Dimension(36, 36));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadListings();
        });
        searchBox.add(clearBtn);

        ModernTheme.stylePrimaryButton(searchBtn);
        searchBtn.setPreferredSize(new Dimension(90, 36));
        searchBtn.addActionListener(e -> loadListings());
        searchBox.add(searchBtn);

        bar.add(searchBox, BorderLayout.WEST);

        // Right: Sort, Post Listing & Refresh
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBox.setOpaque(false);

        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setFont(ModernTheme.FONT_BODY_BOLD);
        sortLbl.setForeground(ModernTheme.TEXT_MUTED);
        rightBox.add(sortLbl);

        ModernTheme.styleComboBox(sortCombo);
        sortCombo.setPreferredSize(new Dimension(160, 36));
        sortCombo.addActionListener(e -> loadListings());
        rightBox.add(sortCombo);

        ModernTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> {
            loadCategories();
            loadListings();
        });
        rightBox.add(refreshBtn);

        ModernTheme.stylePrimaryButton(addListingBtn);
        addListingBtn.setPreferredSize(new Dimension(130, 36));
        addListingBtn.addActionListener(e -> openAddListingDialog());
        rightBox.add(addListingBtn);

        bar.add(rightBox, BorderLayout.EAST);
        return bar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ModernTheme.SURFACE);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, ModernTheme.BORDER),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Filters Header
        JLabel filtersTitle = new JLabel("🎛️ FILTERS");
        filtersTitle.setFont(ModernTheme.FONT_TITLE);
        filtersTitle.setForeground(Color.WHITE);
        filtersTitle.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(filtersTitle);
        sidebar.add(Box.createVerticalStrut(14));

        // Categories List Panel
        categorySidebar.setLayout(new BoxLayout(categorySidebar, BoxLayout.Y_AXIS));
        categorySidebar.setOpaque(false);
        categorySidebar.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(categorySidebar);

        sidebar.add(Box.createVerticalStrut(20));

        // Promo Exam Hub card matching screenshot
        JPanel promoBox = new JPanel();
        promoBox.setLayout(new BoxLayout(promoBox, BoxLayout.Y_AXIS));
        promoBox.setBackground(ModernTheme.ACCENT_ORANGE);
        promoBox.setBorder(new EmptyBorder(14, 14, 14, 14));
        promoBox.setAlignmentX(LEFT_ALIGNMENT);
        promoBox.setMaximumSize(new Dimension(210, 180));

        JLabel promoTitle = new JLabel("NEED STUDY RESOURCES?");
        promoTitle.setFont(ModernTheme.FONT_BODY_BOLD);
        promoTitle.setForeground(Color.WHITE);
        promoBox.add(promoTitle);
        promoBox.add(Box.createVerticalStrut(6));

        JLabel promoDesc = new JLabel("<html>Get PYQs, syllabus, and exam prep materials in our Exam Hub.</html>");
        promoDesc.setFont(ModernTheme.FONT_SMALL);
        promoDesc.setForeground(Color.WHITE);
        promoBox.add(promoDesc);
        promoBox.add(Box.createVerticalStrut(10));

        JButton promoBtn = new JButton("VISIT EXAM HUB");
        promoBtn.setFont(ModernTheme.FONT_SMALL_BOLD);
        promoBtn.setBackground(new Color(24, 24, 27));
        promoBtn.setForeground(Color.WHITE);
        promoBtn.setFocusPainted(false);
        promoBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
        promoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        promoBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Exam Hub: Digital repository with 5 years semester PYQs, solved question banks, and class notes.",
                "Exam Hub Resources", JOptionPane.INFORMATION_MESSAGE));
        promoBox.add(promoBtn);

        sidebar.add(promoBox);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    public void initData() {
        loadCategories();
        loadListings();
    }

    private void loadCategories() {
        SwingWorkerHelper.execute(MarketplaceService::getCategories, cats -> {
            this.categories = cats;
            rebuildCategoryButtons();
        }, error -> statusLabel.setText("Failed to load categories: " + error.getMessage()));
    }

    private void rebuildCategoryButtons() {
        categorySidebar.removeAll();
        ButtonGroup group = new ButtonGroup();

        // "ALL" Option
        JRadioButton allBtn = createCategoryRadio("ALL", 0, selectedCategoryId == 0);
        group.add(allBtn);
        categorySidebar.add(allBtn);
        categorySidebar.add(Box.createVerticalStrut(4));

        for (Category cat : categories) {
            String label = cat.getName();
            if (label.length() > 22) {
                label = label.substring(0, 20) + "..";
            }
            JRadioButton radio = createCategoryRadio(label, cat.getId(), selectedCategoryId == cat.getId());
            group.add(radio);
            categorySidebar.add(radio);
            categorySidebar.add(Box.createVerticalStrut(4));
        }

        categorySidebar.revalidate();
        categorySidebar.repaint();
    }

    private JRadioButton createCategoryRadio(String title, int id, boolean selected) {
        JRadioButton btn = new JRadioButton(title, selected);
        btn.setFont(ModernTheme.FONT_BODY_BOLD);
        btn.setForeground(selected ? Color.WHITE : ModernTheme.TEXT_MUTED);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            this.selectedCategoryId = id;
            rebuildCategoryButtons();
            loadListings();
        });
        return btn;
    }

    public void loadListings() {
        statusLabel.setText("Fetching listings...");
        String query = searchField.getText().trim();
        Integer catId = selectedCategoryId > 0 ? selectedCategoryId : null;
        String sortOption = switch (sortCombo.getSelectedIndex()) {
            case 1 -> "price_asc";
            case 2 -> "price_desc";
            case 3 -> "views_desc";
            default -> "newest";
        };

        SwingWorkerHelper.execute(() -> MarketplaceService.getListings(query, catId, selectedCondition, null, null, sortOption), listings -> {
            this.currentListings = listings;
            statusLabel.setText("");
            resultsCountLabel.setText("Showing " + listings.size() + " academic items");
            renderCards();
        }, error -> {
            statusLabel.setText("Error: " + error.getMessage());
            JOptionPane.showMessageDialog(this, error.getMessage(), "Catalog Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void renderCards() {
        cardsGrid.removeAll();

        if (currentListings.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(new EmptyBorder(80, 40, 80, 40));

            JLabel emptyIcon = new JLabel("📦");
            emptyIcon.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 48));
            emptyIcon.setAlignmentX(CENTER_ALIGNMENT);
            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(10));

            JLabel emptyTitle = new JLabel("No matching campus items found");
            emptyTitle.setFont(ModernTheme.FONT_TITLE);
            emptyTitle.setForeground(Color.WHITE);
            emptyTitle.setAlignmentX(CENTER_ALIGNMENT);
            emptyPanel.add(emptyTitle);
            emptyPanel.add(Box.createVerticalStrut(6));

            JLabel emptySub = new JLabel("Try searching with different keywords or selecting another category filter.");
            emptySub.setFont(ModernTheme.FONT_BODY);
            emptySub.setForeground(ModernTheme.TEXT_MUTED);
            emptySub.setAlignmentX(CENTER_ALIGNMENT);
            emptyPanel.add(emptySub);

            cardsGrid.add(emptyPanel);
        } else {
            for (Listing listing : currentListings) {
                ProductCardPanel card = new ProductCardPanel(listing,
                        this::handleChat,
                        this::handleDetails,
                        this::handleWishlistToggle);
                cardsGrid.add(card);
            }
        }

        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private void handleChat(Listing item) {
        if (!AppConfig.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in to chat with student sellers.", "Login Required", JOptionPane.WARNING_MESSAGE);
            mainFrame.showScreen("LOGIN");
            return;
        }

        String message = JOptionPane.showInputDialog(this,
                "Send Instant Message to " + item.getSellerName() + " (" + item.getSeller().getEmail() + "):\nItem: " + item.getTitle() + " (₹" + (int)item.getPrice() + ")",
                "Chat with Seller",
                JOptionPane.PLAIN_MESSAGE);

        if (message != null && !message.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Message sent to " + item.getSellerName() + "!\nThey will receive your contact details (" + AppConfig.getCurrentUser().getEmail() + ").",
                    "Message Delivered", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDetails(Listing item) {
        ProductDetailsDialog dialog = new ProductDetailsDialog(mainFrame, item, this::handleWishlistToggle);
        dialog.setVisible(true);
    }

    private void handleWishlistToggle(Listing item) {
        if (!AppConfig.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in to save items to your wishlist.", "Login Required", JOptionPane.WARNING_MESSAGE);
            mainFrame.showScreen("LOGIN");
            return;
        }

        SwingWorkerHelper.execute(() -> {
            MarketplaceService.addToWishlist(item.getId());
            return null;
        }, res -> JOptionPane.showMessageDialog(this,
                "\"" + item.getTitle() + "\" has been saved to your Wishlist!",
                "Saved to Wishlist", JOptionPane.INFORMATION_MESSAGE),
        error -> JOptionPane.showMessageDialog(this, error.getMessage(), "Wishlist", JOptionPane.INFORMATION_MESSAGE));
    }

    private void openAddListingDialog() {
        if (!AppConfig.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in to post a marketplace listing.", "Login Required", JOptionPane.WARNING_MESSAGE);
            mainFrame.showScreen("LOGIN");
            return;
        }

        if (!AppConfig.isVerifiedStudent()) {
            JOptionPane.showMessageDialog(this,
                    "Account verification required!\nYour student registration is pending administrator approval before you can create listings.",
                    "Pending Approval", JOptionPane.WARNING_MESSAGE);
            mainFrame.showScreen("PENDING_VERIFICATION");
            return;
        }

        AddListingDialog dialog = new AddListingDialog(mainFrame, categories, () -> {
            loadListings();
            JOptionPane.showMessageDialog(this, "Your listing has been posted successfully!", "Listing Live", JOptionPane.INFORMATION_MESSAGE);
        });
        dialog.setVisible(true);
    }
}
