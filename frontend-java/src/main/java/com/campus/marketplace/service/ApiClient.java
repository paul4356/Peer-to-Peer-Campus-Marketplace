package com.campus.marketplace.service;

import com.campus.marketplace.config.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    public static class ApiException extends Exception {
        private final int statusCode;
        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        public int getStatusCode() { return statusCode; }
    }

    public static Gson getGson() {
        return gson;
    }

    public static JsonObject get(String endpoint) throws IOException, InterruptedException, ApiException {
        HttpRequest.Builder builder = createRequestBuilder(endpoint)
                .GET();
        return execute(builder);
    }

    public static JsonObject post(String endpoint, Object payload) throws IOException, InterruptedException, ApiException {
        String json = payload != null ? (payload instanceof String ? (String)payload : gson.toJson(payload)) : "{}";
        HttpRequest.Builder builder = createRequestBuilder(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(json));
        return execute(builder);
    }

    public static JsonObject put(String endpoint, Object payload) throws IOException, InterruptedException, ApiException {
        String json = payload != null ? (payload instanceof String ? (String)payload : gson.toJson(payload)) : "{}";
        HttpRequest.Builder builder = createRequestBuilder(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(json));
        return execute(builder);
    }

    public static JsonObject patch(String endpoint, Object payload) throws IOException, InterruptedException, ApiException {
        String json = payload != null ? (payload instanceof String ? (String)payload : gson.toJson(payload)) : "{}";
        HttpRequest.Builder builder = createRequestBuilder(endpoint)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json));
        return execute(builder);
    }

    public static JsonObject delete(String endpoint) throws IOException, InterruptedException, ApiException {
        HttpRequest.Builder builder = createRequestBuilder(endpoint)
                .DELETE();
        return execute(builder);
    }

    private static HttpRequest.Builder createRequestBuilder(String endpoint) {
        String url = AppConfig.getApiBaseUrl() + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");

        String token = AppConfig.getAuthToken();
        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    private static JsonObject execute(HttpRequest.Builder builder) throws IOException, InterruptedException, ApiException {
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body();

        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            if (status >= 400) {
                throw new ApiException("Server returned HTTP " + status + ": " + body, status);
            }
            throw new ApiException("Invalid JSON response from server.", status);
        }

        if (status >= 400) {
            String errorMsg = "Request failed with HTTP " + status;
            if (json.has("error") && !json.get("error").isJsonNull()) {
                errorMsg = json.get("error").getAsString();
            } else if (json.has("message") && !json.get("message").isJsonNull()) {
                errorMsg = json.get("message").getAsString();
            }
            throw new ApiException(errorMsg, status);
        }

        return json;
    }
}
