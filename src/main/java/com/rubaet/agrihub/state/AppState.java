package com.rubaet.agrihub.state;

import javafx.stage.Stage;
import java.util.Base64;

/**
 * Singleton class to manage the global state of the JavaFX Application.
 * Stores the primary Stage so all navigation routes through one window,
 * eliminating the need for new Stage() calls in controllers.
 */
public class AppState {

    private static AppState instance;

    private String selectedCropName;
    private String jwtToken;
    private String refreshToken;
    private String userEmail;

    /** The single application window. Set once in Start.java. */
    private Stage primaryStage;

    private AppState() {
    }

    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public String getSelectedCropName() {
        return selectedCropName;
    }

    public void setSelectedCropName(String selectedCropName) {
        this.selectedCropName = selectedCropName;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    /** Stores the JWT token and automatically decodes the user email from it. */
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
        this.userEmail = extractEmailFromJwt(jwtToken);
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getUserEmail() {
        return userEmail != null ? userEmail : "";
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    /**
     * Extracts the "sub" (email) claim from a JWT token without an external library.
     * JWT format: base64(header).base64(payload).base64(signature)
     */
    private String extractEmailFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            // Add padding if needed for Base64 decoding
            String payload = parts[1];
            int padLen = (4 - payload.length() % 4) % 4;
            payload += "=".repeat(padLen);
            String decoded = new String(Base64.getDecoder().decode(payload));
            // Parse the "sub" field from raw JSON without a library
            if (decoded.contains("\"sub\"")) {
                int start = decoded.indexOf("\"sub\"") + 6; // after "sub":
                start = decoded.indexOf("\"", start) + 1;
                int end = decoded.indexOf("\"", start);
                return decoded.substring(start, end);
            }
        } catch (Exception ignored) {}
        return null;
    }

}
