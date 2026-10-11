package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class AdminMetrics {
    @SerializedName("total_users")
    private int totalUsers;
    @SerializedName("approved_students")
    private int approvedStudents;
    @SerializedName("pending_verifications")
    private int pendingVerifications;
    @SerializedName("rejected_verifications")
    private int rejectedVerifications;
    @SerializedName("total_listings")
    private int totalListings;
    @SerializedName("available_listings")
    private int availableListings;
    @SerializedName("sold_listings")
    private int soldListings;
    @SerializedName("total_views")
    private int totalViews;
    @SerializedName("total_reports")
    private int totalReports;
    @SerializedName("pending_reports")
    private int pendingReports;

    public int getTotalUsers() { return totalUsers; }
    public int getApprovedStudents() { return approvedStudents; }
    public int getPendingVerifications() { return pendingVerifications; }
    public int getRejectedVerifications() { return rejectedVerifications; }
    public int getTotalListings() { return totalListings; }
    public int getAvailableListings() { return availableListings; }
    public int getSoldListings() { return soldListings; }
    public int getTotalViews() { return totalViews; }
    public int getTotalReports() { return totalReports; }
    public int getPendingReports() { return pendingReports; }
}
