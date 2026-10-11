package com.campus.marketplace.service;

import com.campus.marketplace.models.Category;
import com.campus.marketplace.models.Listing;
import com.campus.marketplace.models.SellerStats;
import com.campus.marketplace.models.WishlistItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketplaceService {
    private static final Gson gson = ApiClient.getGson();

    public static List<Category> getCategories() throws Exception {
        JsonObject res = ApiClient.get("/api/categories");
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("categories");
        Type listType = new TypeToken<List<Category>>() {}.getType();
        return gson.fromJson(arr, listType);
    }

    public static List<Listing> getListings(String query, Integer categoryId, String condition,
                                            Double minPrice, Double maxPrice, String sort) throws Exception {
        StringBuilder url = new StringBuilder("/api/listings?");
        if (query != null && !query.trim().isEmpty()) {
            url.append("q=").append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8)).append("&");
        }
        if (categoryId != null && categoryId > 0) {
            url.append("category_id=").append(categoryId).append("&");
        }
        if (condition != null && !condition.trim().isEmpty() && !"ALL".equalsIgnoreCase(condition)) {
            url.append("condition=").append(URLEncoder.encode(condition.trim(), StandardCharsets.UTF_8)).append("&");
        }
        if (minPrice != null && minPrice >= 0) {
            url.append("min_price=").append(minPrice).append("&");
        }
        if (maxPrice != null && maxPrice > 0) {
            url.append("max_price=").append(maxPrice).append("&");
        }
        if (sort != null && !sort.trim().isEmpty()) {
            url.append("sort=").append(sort).append("&");
        }

        JsonObject res = ApiClient.get(url.toString());
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("listings");
        Type listType = new TypeToken<List<Listing>>() {}.getType();
        return gson.fromJson(arr, listType);
    }

    public static Listing getListing(int id) throws Exception {
        JsonObject res = ApiClient.get("/api/listings/" + id);
        JsonObject obj = res.getAsJsonObject("data").getAsJsonObject("listing");
        return gson.fromJson(obj, Listing.class);
    }

    public static int createListing(String title, String description, double price, int categoryId,
                                    String conditionState, String imageUrl, String contactPref) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("price", price);
        payload.put("category_id", categoryId);
        payload.put("condition_state", conditionState);
        payload.put("image_url", imageUrl);
        payload.put("contact_preference", contactPref);

        JsonObject res = ApiClient.post("/api/listings", payload);
        return res.getAsJsonObject("data").get("id").getAsInt();
    }

    public static void updateListing(int id, String title, String description, double price, int categoryId,
                                     String conditionState, String imageUrl, String contactPref) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("price", price);
        payload.put("category_id", categoryId);
        payload.put("condition_state", conditionState);
        payload.put("image_url", imageUrl);
        payload.put("contact_preference", contactPref);

        ApiClient.put("/api/listings/" + id, payload);
    }

    public static void deleteListing(int id) throws Exception {
        ApiClient.delete("/api/listings/" + id);
    }

    public static void updateListingStatus(int id, String status) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        ApiClient.patch("/api/listings/" + id + "/status", payload);
    }

    public static List<Listing> getMyListings() throws Exception {
        JsonObject res = ApiClient.get("/api/me/listings");
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("listings");
        Type listType = new TypeToken<List<Listing>>() {}.getType();
        return gson.fromJson(arr, listType);
    }

    public static List<WishlistItem> getWishlist() throws Exception {
        JsonObject res = ApiClient.get("/api/me/wishlist");
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("wishlist");
        Type listType = new TypeToken<List<WishlistItem>>() {}.getType();
        return gson.fromJson(arr, listType);
    }

    public static void addToWishlist(int listingId) throws Exception {
        ApiClient.post("/api/me/wishlist/" + listingId, null);
    }

    public static void removeFromWishlist(int listingId) throws Exception {
        ApiClient.delete("/api/me/wishlist/" + listingId);
    }

    public static SellerStats getSellerDashboard() throws Exception {
        JsonObject res = ApiClient.get("/api/seller/dashboard");
        JsonObject statsObj = res.getAsJsonObject("data").getAsJsonObject("stats");
        return gson.fromJson(statsObj, SellerStats.class);
    }

    public static void reportListing(int listingId, String reason, String details) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("listing_id", listingId);
        payload.put("reason", reason);
        payload.put("details", details);
        ApiClient.post("/api/reports", payload);
    }
}
