package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class SellerStats {
    @SerializedName("total_listings")
    private int totalListings;
    @SerializedName("active_listings")
    private int activeListings;
    @SerializedName("sold_listings")
    private int soldListings;
    @SerializedName("reserved_listings")
    private int reservedListings;
    @SerializedName("total_revenue")
    private double totalRevenue;
    @SerializedName("total_views")
    private int totalViews;

    public int getTotalListings() { return totalListings; }
    public int getActiveListings() { return activeListings; }
    public int getSoldListings() { return soldListings; }
    public int getReservedListings() { return reservedListings; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getTotalViews() { return totalViews; }
}
