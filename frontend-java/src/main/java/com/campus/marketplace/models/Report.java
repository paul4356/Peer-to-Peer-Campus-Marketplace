package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class Report {
    private int id;
    private String reason;
    private String details;
    private String status; // "pending", "reviewed", "dismissed"
    @SerializedName("admin_notes")
    private String adminNotes;
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("listing_id")
    private int listingId;
    @SerializedName("listing_title")
    private String listingTitle;
    @SerializedName("listing_price")
    private double listingPrice;
    @SerializedName("listing_status")
    private String listingStatus;

    @SerializedName("reporter_id")
    private int reporterId;
    @SerializedName("reporter_name")
    private String reporterName;
    @SerializedName("reporter_email")
    private String reporterEmail;

    @SerializedName("seller_id")
    private int sellerId;
    @SerializedName("seller_name")
    private String sellerName;
    @SerializedName("seller_email")
    private String sellerEmail;

    public int getId() { return id; }
    public String getReason() { return reason != null ? reason : ""; }
    public String getDetails() { return details != null ? details : ""; }
    public String getStatus() { return status != null ? status : "pending"; }
    public String getAdminNotes() { return adminNotes; }
    public String getCreatedAt() { return createdAt; }

    public int getListingId() { return listingId; }
    public String getListingTitle() { return listingTitle != null ? listingTitle : ""; }
    public double getListingPrice() { return listingPrice; }
    public String getListingStatus() { return listingStatus; }

    public int getReporterId() { return reporterId; }
    public String getReporterName() { return reporterName != null ? reporterName : "Student"; }
    public String getReporterEmail() { return reporterEmail; }

    public int getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName != null ? sellerName : "Student"; }
    public String getSellerEmail() { return sellerEmail; }
}
