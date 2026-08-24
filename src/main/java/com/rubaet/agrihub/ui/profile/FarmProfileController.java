package com.rubaet.agrihub.ui.profile;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.service.ApiService;
import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class FarmProfileController implements Initializable {

    @FXML private TextField farmNameField;
    @FXML private TextField locationField;
    @FXML private TextField soilTypeField;
    @FXML private TextField areaField;
    @FXML private TextField primaryCropsField;
    @FXML private TextField regionField;
    @FXML private Button saveBtn;
    @FXML private Label statusBadge;
    @FXML private Label resultLabel;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusBadge.setText("Loading...");
        loadProfile();
    }

    private void loadProfile() {
        ApiService.getFarmProfile().thenAccept(body -> {
            Platform.runLater(() -> {
                try {
                    JsonNode root = MAPPER.readTree(body);
                    populateField(farmNameField,    root, "farmName");
                    populateField(locationField,    root, "location");
                    populateField(soilTypeField,    root, "soilType");
                    populateField(primaryCropsField,root, "primaryCrops");
                    populateField(regionField,      root, "region");
                    if (root.has("areaHectares") && !root.get("areaHectares").isNull()) {
                        areaField.setText(String.valueOf(root.get("areaHectares").asDouble()));
                    }
                    boolean hasData = root.has("farmName") && !root.get("farmName").isNull();
                    statusBadge.setText(hasData ? "✅ Profile Saved" : "⚠️ Not Set Up");
                    statusBadge.setStyle(hasData
                        ? "-fx-text-fill: #22c55e; -fx-background-color: #1a3d2b; -fx-background-radius: 9999; -fx-padding: 3 10 3 10;"
                        : "-fx-text-fill: #f59e0b; -fx-background-color: #3d2e0d; -fx-background-radius: 9999; -fx-padding: 3 10 3 10;");
                } catch (Exception e) {
                    statusBadge.setText("⚠️ Not Set Up");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> statusBadge.setText("⚠️ Offline"));
            return null;
        });
    }

    @FXML
    public void saveProfile(ActionEvent event) {
        String area = areaField.getText().trim();
        double areaHectares = 0.0;
        try {
            if (!area.isEmpty()) areaHectares = Double.parseDouble(area);
        } catch (NumberFormatException e) {
            resultLabel.setText("⚠️ Area must be a number (e.g. 5.5)");
            return;
        }

        saveBtn.setDisable(true);
        saveBtn.setText("⏳ Saving...");
        resultLabel.setText("");

        ApiService.saveFarmProfile(
            farmNameField.getText().trim(),
            locationField.getText().trim(),
            soilTypeField.getText().trim(),
            areaHectares,
            primaryCropsField.getText().trim(),
            regionField.getText().trim()
        ).thenAccept(body -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                saveBtn.setText("💾  Save Profile");
                resultLabel.setText("✅ Profile saved! AgriBot now knows your farm.");
                statusBadge.setText("✅ Profile Saved");
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                saveBtn.setDisable(false);
                saveBtn.setText("💾  Save Profile");
                resultLabel.setText("❌ Failed to save. Is the backend running?");
            });
            return null;
        });
    }

    @FXML
    public void goHome() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml",
            "Agri-Hub — Dashboard"
        );
    }

    private void populateField(TextField field, JsonNode root, String key) {
        if (root.has(key) && !root.get(key).isNull()) {
            field.setText(root.get(key).asText());
        }
    }
}
