package com.campus.marketplace.config;

import com.campus.marketplace.models.User;
import java.util.ArrayList;
import java.util.List;

public class AppConfig {
    public static final String APP_TITLE = "Peer-to-Peer Campus Marketplace";
    public static final String APP_VERSION = "v1.0.0";
    
    // Configurable API Base URL
    private static String apiBaseUrl = System.getProperty("api.base.url", "http://127.0.0.1:8000");

    private static String authToken = null;
    private static User currentUser = null;

    public interface AuthStateListener {
        void onAuthStateChanged(User user, boolean loggedIn);
    }

    private static final List<AuthStateListener> authListeners = new ArrayList<>();

    public static synchronized String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public static synchronized void setApiBaseUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            apiBaseUrl = url.trim().replaceAll("/+$", "");
        }
    }

    public static synchronized String getAuthToken() {
        return authToken;
    }

    public static synchronized void setAuthToken(String token) {
        authToken = token;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        notifyAuthListeners();
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null && authToken != null;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static synchronized boolean isVerifiedStudent() {
        return currentUser != null && (currentUser.isAdmin() || currentUser.isApproved());
    }

    public static synchronized void logout() {
        authToken = null;
        currentUser = null;
        notifyAuthListeners();
    }

    public static synchronized void addAuthStateListener(AuthStateListener listener) {
        authListeners.add(listener);
    }

    private static void notifyAuthListeners() {
        boolean loggedIn = isLoggedIn();
        for (AuthStateListener listener : authListeners) {
            listener.onAuthStateChanged(currentUser, loggedIn);
        }
    }
}
