package com.rubaet.agrihub.service;

import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Centralized async HTTP service for the Spring Boot backend.
 * All methods return CompletableFuture and are non-blocking.
 */
public class ApiService {

    private static final String API_KEY = resolveApiKey();
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String resolveApiKey() {
        String key = System.getProperty("agrihub.api.key");
        if (key != null && !key.isEmpty()) return key;
        key = System.getenv("AGRIHUB_API_KEY");
        if (key != null && !key.isEmpty()) return key;
        return "agri_hub_desktop_client_secret_2026"; // local dev fallback
    }

    // ── Generic HTTP Helpers ─────────────────────────────────────

    private static CompletableFuture<String> get(String path) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("X-API-KEY", API_KEY)
                .GET();
        addJwt(rb);
        return CLIENT.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                     .thenApply(HttpResponse::body);
    }

    private static CompletableFuture<String> post(String path, Map<String, Object> body) {
        try {
            String json = MAPPER.writeValueAsString(body);
            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("X-API-KEY", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            addJwt(rb);
            return CLIENT.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                         .thenApply(HttpResponse::body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static CompletableFuture<String> postEmpty(String path) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("X-API-KEY", API_KEY)
                .POST(HttpRequest.BodyPublishers.noBody());
        addJwt(rb);
        return CLIENT.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                     .thenApply(HttpResponse::body);
    }

    private static CompletableFuture<String> put(String path) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("X-API-KEY", API_KEY)
                .PUT(HttpRequest.BodyPublishers.noBody());
        addJwt(rb);
        return CLIENT.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                     .thenApply(HttpResponse::body);
    }

    private static CompletableFuture<String> delete(String path) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("X-API-KEY", API_KEY)
                .DELETE();
        addJwt(rb);
        return CLIENT.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofString())
                     .thenApply(HttpResponse::body);
    }

    private static void addJwt(HttpRequest.Builder rb) {
        String token = AppState.getInstance().getJwtToken();
        if (token != null && !token.isEmpty()) {
            rb.header("Authorization", "Bearer " + token);
        }
    }

    // ── Auth ─────────────────────────────────────────────────────

    public static CompletableFuture<String[]> login(String email, String password) {
        return authRequest("/auth/login", email, password);
    }

    public static CompletableFuture<String[]> register(String email, String password) {
        return authRequest("/auth/register", email, password);
    }

    private static CompletableFuture<String[]> authRequest(String endpoint, String email, String password) {
        try {
            String json = MAPPER.writeValueAsString(Map.of("email", email, "password", password));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("X-API-KEY", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
                if (res.statusCode() == 200) {
                    try {
                        JsonNode root = MAPPER.readTree(res.body());
                        String token   = root.path("token").asText(null);
                        String refresh = root.path("refreshToken").asText(null);
                        return new String[]{token, refresh};
                    } catch (Exception ignored) {}
                }
                return null;
            });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // ── AI ────────────────────────────────────────────────────────

    public static CompletableFuture<String> askAi(String prompt, List<Map<String, String>> history) {
        return sendAiPost("/ai/ask", prompt, history);
    }

    public static CompletableFuture<String> askAiCropPlan(String userInput, String ignored) {
        return sendAiPost("/ai/plan-crop", userInput, null);
    }

    public static CompletableFuture<String> askAiDisease(String symptoms) {
        return sendAiPost("/ai/identify-disease", "Symptoms: " + symptoms, null);
    }

    private static CompletableFuture<String> sendAiPost(String endpoint, String prompt,
                                                         List<Map<String, String>> history) {
        Map<String, Object> body = history != null && !history.isEmpty()
            ? Map.of("prompt", prompt, "history", history)
            : Map.of("prompt", prompt);
        return post(endpoint, body);
    }

    // ── Weather ──────────────────────────────────────────────────

    public static CompletableFuture<String> getWeatherData(String city) {
        return get("/weather?city=" + city.replace(" ", "%20"));
    }

    // ── Crop Data ────────────────────────────────────────────────

    public static CompletableFuture<String> getCropData(String cropName) {
        String name = cropName.endsWith(" Summary") ? cropName : cropName + " Summary";
        return get("/crops/search?name=" + name.replace(" ", "%20"));
    }

    // ── Farm Profile ─────────────────────────────────────────────

    public static CompletableFuture<String> getFarmProfile() {
        return get("/profile/me");
    }

    public static CompletableFuture<String> saveFarmProfile(
            String farmName, String location, String soilType,
            double areaHectares, String primaryCrops, String region) {
        return post("/profile", Map.of(
            "farmName", farmName, "location", location,
            "soilType", soilType, "areaHectares", areaHectares,
            "primaryCrops", primaryCrops, "region", region));
    }

    // ── Analytics ────────────────────────────────────────────────

    public static CompletableFuture<String> getYieldEstimate(String crop, double areaHectares, double seasonFactor) {
        return post("/analytics/yield-estimate", Map.of(
            "crop", crop, "areaHectares", areaHectares, "seasonYieldFactor", seasonFactor));
    }

    // ── Crop Diary ───────────────────────────────────────────────

    public static CompletableFuture<String> createCropCycle(String cropName, String season, String startDate, String notes) {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("cropName", cropName);
        body.put("season", season);
        body.put("startDate", startDate);
        body.put("notes", notes);
        return post("/diary/cycles", body);
    }

    public static CompletableFuture<String> getCropCycles(int page, int size) {
        return get("/diary/cycles?page=" + page + "&size=" + size);
    }

    public static CompletableFuture<String> completeCropCycle(Long id) {
        return put("/diary/cycles/" + id + "/complete");
    }

    public static CompletableFuture<String> deleteCropCycle(Long id) {
        return delete("/diary/cycles/" + id);
    }

    public static CompletableFuture<String> logActivity(Long cycleId, String activityType, String description, double cost) {
        return post("/diary/cycles/" + cycleId + "/activities", Map.of(
            "activityType", activityType, "description", description, "cost", cost));
    }

    public static CompletableFuture<String> getActivities(Long cycleId) {
        return get("/diary/cycles/" + cycleId + "/activities");
    }

    // ── Alerts ───────────────────────────────────────────────────

    public static CompletableFuture<String> getAlerts() {
        return get("/alerts");
    }

    public static CompletableFuture<String> markAlertRead(Long id) {
        return put("/alerts/" + id + "/read");
    }

    public static CompletableFuture<String> createTestAlert() {
        return postEmpty("/alerts/test");
    }

    // ── Harvest Records ──────────────────────────────────────────

    public static CompletableFuture<String> recordHarvest(String cropName, String season, int year,
                                                           double yieldKg, double revenueUsd, String notes) {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("cropName", cropName);
        body.put("season", season);
        body.put("year", year);
        body.put("actualYieldKg", yieldKg);
        body.put("revenueUsd", revenueUsd);
        body.put("notes", notes);
        return post("/harvest", body);
    }

    public static CompletableFuture<String> getHarvestRecords() {
        return get("/harvest");
    }

    public static CompletableFuture<String> getHarvestSummary() {
        return get("/harvest/summary");
    }
}
