package com.campus.marketplace.ui.dialogs;

import com.campus.marketplace.models.Category;
import com.campus.marketplace.service.MarketplaceService;
import com.campus.marketplace.ui.ModernTheme;
import com.campus.marketplace.util.SwingWorkerHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class AddListingDialog extends JDialog {
    private final JTextField titleField = new JTextField();
    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final JTextField priceField = new JTextField();
    private final JTextArea descArea = new JTextArea(4, 20);
    private final JComboBox<String> conditionBox = new JComboBox<>(new String[]{"New", "Like New", "Good", "Acceptable"});
    private final JComboBox<String> contactBox = new JComboBox<>(new String[]{"chat", "phone", "email"});

    private final JButton submitBtn = new JButton("Publish Listing");
    private final JButton cancelBtn = new JButton("Cancel");
    private final Runnable onSuccess;

    public AddListingDialog(Frame parent, List<Category> categories, Runnable onSuccess) {
        super(parent, "Post New Marketplace Listing", true);
        this.onSuccess = onSuccess;

        setSize(520, 560);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout());

        for (Category c : categories) {
            categoryBox.addItem(c);
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(ModernTheme.SURFACE);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("POST STUDY ESSENTIAL");
        titleLbl.setFont(ModernTheme.FONT_TITLE);
        titleLbl.setForeground(ModernTheme.ACCENT_ORANGE);
        form.add(titleLbl);
        form.add(Box.createVerticalStrut(16));

        // Form fields grid
        JPanel grid = new JPanel(new GridLayout(4, 2, 10, 12));
        grid.setOpaque(false);

        grid.add(createLabel("Item Title / Name: *"));
        ModernTheme.styleTextField(titleField);
        grid.add(titleField);

        grid.add(createLabel("Academic Category: *"));
        ModernTheme.styleComboBox(categoryBox);
        grid.add(categoryBox);

        grid.add(createLabel("Expected Price (₹): *"));
        ModernTheme.styleTextField(priceField);
        grid.add(priceField);

        grid.add(createLabel("Item Condition: *"));
        ModernTheme.styleComboBox(conditionBox);
        conditionBox.setSelectedItem("Good");
        grid.add(conditionBox);

        form.add(grid);
        form.add(Box.createVerticalStrut(14));

        // Description
        JLabel descLbl = createLabel("Item Description & Details: *");
        descLbl.setAlignmentX(LEFT_ALIGNMENT);
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
        form.add(Box.createVerticalStrut(12));

        // Contact preference
        JPanel contactRow = new JPanel(new GridLayout(1, 2, 10, 0));
        contactRow.setOpaque(false);
        contactRow.add(createLabel("Preferred Contact Method:"));
        ModernTheme.styleComboBox(contactBox);
        contactRow.add(contactBox);
        form.add(contactRow);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setBackground(ModernTheme.SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));

        ModernTheme.styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> dispose());
        footer.add(cancelBtn);

        ModernTheme.stylePrimaryButton(submitBtn);
        submitBtn.addActionListener(e -> submitListing());
        footer.add(submitBtn);

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ModernTheme.FONT_BODY_BOLD);
        l.setForeground(ModernTheme.TEXT_PRIMARY);
        return l;
    }

    private void submitListing() {
        String title = titleField.getText().trim();
        Category cat = (Category) categoryBox.getSelectedItem();
        String priceText = priceField.getText().trim();
        String desc = descArea.getText().trim();
        String condition = (String) conditionBox.getSelectedItem();
        String contact = (String) contactBox.getSelectedItem();

        if (title.length() < 4) {
            JOptionPane.showMessageDialog(this, "Title must be at least 4 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cat == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (desc.length() < 10) {
            JOptionPane.showMessageDialog(this, "Description must be at least 10 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        submitBtn.setEnabled(false);
        submitBtn.setText("Publishing...");

        SwingWorkerHelper.execute(() -> MarketplaceService.createListing(title, desc, price, cat.getId(), condition, "assets/items/default.png", contact),
                id -> {
                    dispose();
                    if (onSuccess != null) onSuccess.run();
                },
                error -> {
                    submitBtn.setEnabled(true);
                    submitBtn.setText("Publish Listing");
                    JOptionPane.showMessageDialog(this, error.getMessage(), "Submission Failed", JOptionPane.ERROR_MESSAGE);
                });
    }
}
