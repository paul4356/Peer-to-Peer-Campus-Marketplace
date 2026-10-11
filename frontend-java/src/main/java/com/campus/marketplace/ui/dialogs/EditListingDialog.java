package com.campus.marketplace.ui.dialogs;

import com.campus.marketplace.models.Category;
import com.campus.marketplace.models.Listing;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class EditListingDialog extends JDialog {
    private final Listing listing;
    private final JTextField titleField = new JTextField();
    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final JTextField priceField = new JTextField();
    private final JTextArea descArea = new JTextArea(4, 20);
    private final JComboBox<String> conditionBox = new JComboBox<>(new String[]{"New", "Like New", "Good", "Acceptable"});
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"available", "sold", "reserved", "inactive"});

    private final JButton saveBtn = new JButton("Save Changes");
    private final JButton cancelBtn = new JButton("Cancel");
    private final Runnable onSuccess;

    public EditListingDialog(Frame parent, Listing listing, List<Category> categories, Runnable onSuccess) {
        super(parent, "Edit Listing: " + listing.getTitle(), true);
        this.listing = listing;
        this.onSuccess = onSuccess;

        setSize(520, 580);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout());

        // Populate fields
        titleField.setText(listing.getTitle());
        priceField.setText(String.valueOf(listing.getPrice()));
        descArea.setText(listing.getDescription());
        conditionBox.setSelectedItem(listing.getConditionState());
        statusBox.setSelectedItem(listing.getStatus());

        for (Category c : categories) {
            categoryBox.addItem(c);
            if (listing.getCategory() != null && listing.getCategory().getId() == c.getId()) {
                categoryBox.setSelectedItem(c);
            }
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(ModernTheme.SURFACE);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("EDIT LISTING DETAILS");
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        titleLbl.setForeground(ModernTheme.ACCENT_ORANGE);
        form.add(titleLbl);
        form.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(5, 2, 10, 12));
        grid.setOpaque(false);

        grid.add(createLabel("Item Title: *"));
        ModernTheme.styleTextField(titleField);
        grid.add(titleField);

        grid.add(createLabel("Category: *"));
        ModernTheme.styleComboBox(categoryBox);
        grid.add(categoryBox);

        grid.add(createLabel("Price (₹): *"));
        ModernTheme.styleTextField(priceField);
        grid.add(priceField);

        grid.add(createLabel("Condition: *"));
        ModernTheme.styleComboBox(conditionBox);
        grid.add(conditionBox);

        grid.add(createLabel("Listing Status:"));
        ModernTheme.styleComboBox(statusBox);
        grid.add(statusBox);

        form.add(grid);
        form.add(Box.createVerticalStrut(14));

        JLabel descLbl = createLabel("Description: *");
        form.add(descLbl);
        form.add(Box.createVerticalStrut(6));

        descArea.setFont(ModernTheme.FONT_BODY);
        descArea.setForeground(ModernTheme.TEXT_PRIMARY);
        descArea.setBackground(ModernTheme.CARD_BG);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(460, 100));
        form.add(descScroll);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setBackground(ModernTheme.SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));

        ModernTheme.styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> dispose());
        footer.add(cancelBtn);

        ModernTheme.stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> saveListing());
        footer.add(saveBtn);

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ModernTheme.FONT_BODY_BOLD);
        l.setForeground(ModernTheme.TEXT_PRIMARY);
        return l;
    }

    private void saveListing() {
        String title = titleField.getText().trim();
        Category cat = (Category) categoryBox.getSelectedItem();
        String priceText = priceField.getText().trim();
        String desc = descArea.getText().trim();
        String condition = (String) conditionBox.getSelectedItem();
        String status = (String) statusBox.getSelectedItem();

        if (title.length() < 4) {
            JOptionPane.showMessageDialog(this, "Title must be at least 4 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        saveBtn.setEnabled(false);
        saveBtn.setText("Saving...");

        SwingWorkerHelper.execute(() -> {
            MarketplaceService.updateListing(listing.getId(), title, desc, price, cat != null ? cat.getId() : 1, condition, listing.getImageUrl(), "chat");
            MarketplaceService.updateListingStatus(listing.getId(), status);
            return null;
        }, res -> {
            dispose();
            if (onSuccess != null) onSuccess.run();
        }, error -> {
            saveBtn.setEnabled(true);
            saveBtn.setText("Save Changes");
            JOptionPane.showMessageDialog(this, error.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
