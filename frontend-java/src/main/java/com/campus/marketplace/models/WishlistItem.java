package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class WishlistItem {
    @SerializedName("wishlist_id")
    private int wishlistId;
    @SerializedName("saved_at")
    private String savedAt;

    private int id;
    private String title;
    private String description;
    private double price;
    @SerializedName("condition_state")
    private String conditionState;
    @SerializedName("image_url")
    private String imageUrl;
    private String status;
    @SerializedName("views_count")
    private int viewsCount;
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("category_id")
    private int categoryId;
    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("seller_id")
    private int sellerId;
    @SerializedName("seller_name")
    private String sellerName;
    @SerializedName("seller_email")
    private String sellerEmail;
    @SerializedName("seller_phone")
    private String sellerPhone;

    public int getWishlistId() { return wishlistId; }
    public String getSavedAt() { return savedAt; }

    public int getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public double getPrice() { return price; }
    public String getConditionState() { return conditionState != null ? conditionState : "Good"; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status != null ? status : "available"; }
    public int getViewsCount() { return viewsCount; }
    public String getCreatedAt() { return createdAt; }

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName != null ? categoryName : "General"; }

    public int getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName != null ? sellerName : "Student"; }
    public String getSellerEmail() { return sellerEmail != null ? sellerEmail : ""; }
    public String getSellerPhone() { return sellerPhone != null ? sellerPhone : ""; }
}
