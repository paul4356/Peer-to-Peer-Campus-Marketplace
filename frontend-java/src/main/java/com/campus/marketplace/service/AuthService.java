package com.campus.marketplace.service;

import com.campus.marketplace.config.AppConfig;
import com.campus.marketplace.models.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final Gson gson = ApiClient.getGson();

    public static User login(String email, String password) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);

        JsonObject res = ApiClient.post("/api/auth/login", payload);
        JsonObject data = res.getAsJsonObject("data");
        String token = data.get("token").getAsString();
        User user = gson.fromJson(data.get("user"), User.class);

        AppConfig.setAuthToken(token);
        AppConfig.setCurrentUser(user);

        return user;
    }

    public static User register(String studentId, String fullName, String email, String password,
                                 String phone, String department, int yearOfStudy) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("student_id", studentId);
        payload.put("full_name", fullName);
        payload.put("email", email);
        payload.put("password", password);
        payload.put("phone", phone);
        payload.put("department", department);
        payload.put("year_of_study", yearOfStudy);

        JsonObject res = ApiClient.post("/api/auth/register", payload);
        JsonObject data = res.getAsJsonObject("data");
        String token = data.get("token").getAsString();
        User user = gson.fromJson(data.get("user"), User.class);

        AppConfig.setAuthToken(token);
        AppConfig.setCurrentUser(user);

        return user;
    }

    public static void logout() {
        try {
            ApiClient.post("/api/auth/logout", null);
        } catch (Exception ignored) {
        } finally {
            AppConfig.logout();
        }
    }

    public static User fetchCurrentUser() throws Exception {
        JsonObject res = ApiClient.get("/api/auth/me");
        JsonObject data = res.getAsJsonObject("data");
        User user = gson.fromJson(data.get("user"), User.class);
        AppConfig.setCurrentUser(user);
        return user;
    }

    public static User updateProfile(String fullName, String phone, String department, int yearOfStudy, String password) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("full_name", fullName);
        payload.put("phone", phone);
        payload.put("department", department);
        payload.put("year_of_study", yearOfStudy);
        if (password != null && !password.trim().isEmpty()) {
            payload.put("password", password.trim());
        }

        JsonObject res = ApiClient.put("/api/profile", payload);
        JsonObject data = res.getAsJsonObject("data");
        User user = gson.fromJson(data.get("user"), User.class);
        AppConfig.setCurrentUser(user);
        return user;
    }
}
