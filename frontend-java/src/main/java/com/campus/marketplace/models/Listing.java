package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class Listing {
    private int id;
    private String title;
    private String description;
    private double price;
    @SerializedName("condition_state")
    private String conditionState; // "New", "Like New", "Good", "Acceptable"
    @SerializedName("image_url")
    private String imageUrl;
    private String status; // "available", "sold", "reserved", "inactive"
    @SerializedName("views_count")
    private int viewsCount;
    @SerializedName("contact_preference")
    private String contactPreference;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    private Category category;
    private Seller seller;

    public Listing() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getConditionState() { return conditionState != null ? conditionState : "Good"; }
    public void setConditionState(String conditionState) { this.conditionState = conditionState; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status != null ? status : "available"; }
    public void setStatus(String status) { this.status = status; }

    public int getViewsCount() { return viewsCount; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }

    public String getContactPreference() { return contactPreference != null ? contactPreference : "chat"; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }

    public String getCreatedAt() { return createdAt != null ? createdAt : ""; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public String getCategoryName() {
        return category != null ? category.getName() : "General";
    }

    public String getSellerName() {
        return seller != null ? seller.getName() : "Student";
    }

    public boolean isAvailable() {
        return "available".equalsIgnoreCase(status);
    }

    public boolean isSold() {
        return "sold".equalsIgnoreCase(status);
    }

    // Inner class for seller profile nested in listing
    public static class Seller {
        private int id;
        private String name;
        private String email;
        private String phone;
        private String department;
        @SerializedName("verification_status")
        private String verificationStatus;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name != null ? name : ""; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email != null ? email : ""; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone != null ? phone : ""; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getDepartment() { return department != null ? department : ""; }
        public void setDepartment(String department) { this.department = department; }

        public String getVerificationStatus() { return verificationStatus; }
        public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    }
}
