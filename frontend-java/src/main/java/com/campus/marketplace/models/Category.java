package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    private int id;
    private String name;
    private String slug;
    private String description;
    @SerializedName("icon_name")
    private String iconName;
    @SerializedName("active_listings_count")
    private int activeListingsCount;

    public Category() {}

    public Category(int id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug != null ? slug : ""; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getActiveListingsCount() { return activeListingsCount; }
    public void setActiveListingsCount(int count) { this.activeListingsCount = count; }

    @Override
    public String toString() {
        return name;
    }
}
