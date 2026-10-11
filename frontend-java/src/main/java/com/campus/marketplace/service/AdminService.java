package com.campus.marketplace.service;

import com.campus.marketplace.models.AdminMetrics;
import com.campus.marketplace.models.Report;
import com.campus.marketplace.models.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminService {
    private static final Gson gson = ApiClient.getGson();

    public static AdminMetrics getDashboardMetrics() throws Exception {
        JsonObject res = ApiClient.get("/api/admin/dashboard");
        JsonObject metricsObj = res.getAsJsonObject("data").getAsJsonObject("metrics");
        return gson.fromJson(metricsObj, AdminMetrics.class);
    }

    public static List<User> getPendingVerifications() throws Exception {
        JsonObject res = ApiClient.get("/api/admin/verifications");
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("verifications");
        Type listType = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(arr, listType);
    }

    public static void updateVerification(int userId, String status, String reason) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        if (reason != null && !reason.trim().isEmpty()) {
            payload.put("reason", reason.trim());
        }
        ApiClient.patch("/api/admin/users/" + userId + "/verification", payload);
    }

    public static List<User> getAllUsers(String query, String role, String status) throws Exception {
        StringBuilder url = new StringBuilder("/api/admin/users?");
        if (query != null && !query.trim().isEmpty()) {
            url.append("q=").append(query).append("&");
        }
        if (role != null && !role.trim().isEmpty()) {
            url.append("role=").append(role).append("&");
        }
        if (status != null && !status.trim().isEmpty()) {
            url.append("status=").append(status).append("&");
        }

        JsonObject res = ApiClient.get(url.toString());
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("users");
        Type listType = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(arr, listType);
    }

    public static List<Report> getReports() throws Exception {
        JsonObject res = ApiClient.get("/api/admin/reports");
        JsonArray arr = res.getAsJsonObject("data").getAsJsonArray("reports");
        Type listType = new TypeToken<List<Report>>() {}.getType();
        return gson.fromJson(arr, listType);
    }
}
