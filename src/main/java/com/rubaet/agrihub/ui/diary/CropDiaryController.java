package com.rubaet.agrihub.ui.diary;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.service.ApiService;
import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CropDiaryController implements Initializable {

    @FXML private TextField cropNameField;
    @FXML private TextField seasonField;
    @FXML private DatePicker startDatePicker;
    @FXML private TextArea notesField;
    @FXML private Button createCycleBtn;
    @FXML private Label statusLabel;
    @FXML private VBox cycleListBox;

    // Activity log form
    @FXML private TextField activityTypeField;
    @FXML private TextField activityDescField;
    @FXML private TextField activityCostField;
    @FXML private VBox activityListBox;
    @FXML private VBox activityPanel;
    @FXML private Label selectedCycleLabel;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Long selectedCycleId = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startDatePicker.setValue(LocalDate.now());
        activityPanel.setVisible(false);
        activityPanel.setManaged(false);
        loadCycles();
    }

    @FXML
    public void createCycle(ActionEvent event) {
        String crop = cropNameField.getText().trim();
        if (crop.isEmpty()) { statusLabel.setText("⚠ Please enter a crop name."); return; }

        String season = seasonField.getText().trim();
        LocalDate date = startDatePicker.getValue();
        String notes = notesField.getText().trim();

        createCycleBtn.setDisable(true);
        statusLabel.setText("Creating...");

        ApiService.createCropCycle(crop, season, date.toString(), notes).thenAccept(body -> {
            Platform.runLater(() -> {
                createCycleBtn.setDisable(false);
                statusLabel.setText("✅ Cycle created successfully!");
                cropNameField.clear();
                seasonField.clear();
                notesField.clear();
                loadCycles();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                createCycleBtn.setDisable(false);
                statusLabel.setText("❌ Failed: " + e.getMessage());
            });
            return null;
        });
    }

    private void loadCycles() {
        ApiService.getCropCycles(0, 20).thenAccept(body -> {
            Platform.runLater(() -> {
                cycleListBox.getChildren().clear();
                try {
                    JsonNode root = MAPPER.readTree(body);
                    JsonNode cycles = root.path("cycles");
                    if (cycles.isArray() && cycles.size() > 0) {
                        for (JsonNode c : cycles) {
                            cycleListBox.getChildren().add(buildCycleCard(c));
                        }
                    } else {
                        Label empty = new Label("No crop cycles yet. Create one above!");
                        empty.getStyleClass().add("text-muted");
                        cycleListBox.getChildren().add(empty);
                    }
                } catch (Exception e) {
                    Label err = new Label("Failed to load cycles.");
                    err.getStyleClass().add("text-error");
                    cycleListBox.getChildren().add(err);
                }
            });
        });
    }

    private VBox buildCycleCard(JsonNode c) {
        Long id = c.path("id").asLong();
        String crop = c.path("cropName").asText("—");
        String season = c.path("season").asText("—");
        String status = c.path("status").asText("ACTIVE");
        String date = c.path("startDate").asText("—");

        VBox card = new VBox(6);
        card.getStyleClass().add("farm-stat-card");
        card.setPadding(new Insets(12, 16, 12, 16));

        Label title = new Label("🌾 " + crop + " — " + season);
        title.getStyleClass().add("text-card-title");

        Label info = new Label("Started: " + date + "  |  Status: " + status);
        info.getStyleClass().add("text-muted");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn = new Button("📋 Activities");
        viewBtn.getStyleClass().add("btn-ghost");
        viewBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 10;");
        viewBtn.setOnAction(e -> selectCycle(id, crop));

        Button completeBtn = new Button("✅ Complete");
        completeBtn.getStyleClass().add("btn-ghost");
        completeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 10;");
        completeBtn.setOnAction(e -> completeCycle(id));

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-ghost");
        deleteBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 10; -fx-text-fill: #f85149;");
        deleteBtn.setOnAction(e -> deleteCycle(id));

        if ("COMPLETED".equals(status)) {
            completeBtn.setDisable(true);
        }
        actions.getChildren().addAll(viewBtn, completeBtn, deleteBtn);

        card.getChildren().addAll(title, info, actions);
        return card;
    }

    private void selectCycle(Long id, String cropName) {
        selectedCycleId = id;
        selectedCycleLabel.setText("Activities for: " + cropName);
        activityPanel.setVisible(true);
        activityPanel.setManaged(true);
        loadActivities(id);
    }

    @FXML
    public void logActivity(ActionEvent event) {
        if (selectedCycleId == null) return;
        String type = activityTypeField.getText().trim();
        if (type.isEmpty()) { statusLabel.setText("⚠ Activity type required."); return; }

        String desc = activityDescField.getText().trim();
        double cost = 0;
        try { cost = Double.parseDouble(activityCostField.getText().trim()); } catch (Exception ignored) {}

        ApiService.logActivity(selectedCycleId, type, desc, cost).thenAccept(body -> {
            Platform.runLater(() -> {
                statusLabel.setText("✅ Activity logged!");
                activityTypeField.clear();
                activityDescField.clear();
                activityCostField.clear();
                loadActivities(selectedCycleId);
            });
        });
    }

    private void loadActivities(Long cycleId) {
        ApiService.getActivities(cycleId).thenAccept(body -> {
            Platform.runLater(() -> {
                activityListBox.getChildren().clear();
                try {
                    JsonNode arr = MAPPER.readTree(body);
                    if (arr.isArray()) {
                        for (JsonNode a : arr) {
                            HBox row = new HBox(12);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.setPadding(new Insets(6, 0, 6, 0));
                            row.setStyle("-fx-border-color: transparent transparent #30363d transparent; -fx-border-width: 0 0 1 0;");

                            Label typeLabel = new Label(a.path("activityType").asText());
                            typeLabel.getStyleClass().add("text-body");
                            typeLabel.setMinWidth(100);

                            Label descLabel = new Label(a.path("description").asText("—"));
                            descLabel.getStyleClass().add("text-muted");
                            descLabel.setMaxWidth(300);
                            descLabel.setWrapText(true);

                            Label costLabel = new Label("$" + String.format("%.2f", a.path("cost").asDouble()));
                            costLabel.getStyleClass().add("stat-value-warning");
                            costLabel.setStyle("-fx-font-size: 13px;");

                            row.getChildren().addAll(typeLabel, descLabel, costLabel);
                            activityListBox.getChildren().add(row);
                        }
                    }
                    if (activityListBox.getChildren().isEmpty()) {
                        Label empty = new Label("No activities logged yet.");
                        empty.getStyleClass().add("text-muted");
                        activityListBox.getChildren().add(empty);
                    }
                } catch (Exception ignored) {}
            });
        });
    }

    private void completeCycle(Long id) {
        ApiService.completeCropCycle(id).thenAccept(b -> Platform.runLater(this::loadCycles));
    }

    private void deleteCycle(Long id) {
        ApiService.deleteCropCycle(id).thenAccept(b -> Platform.runLater(() -> {
            if (selectedCycleId != null && selectedCycleId.equals(id)) {
                activityPanel.setVisible(false);
                activityPanel.setManaged(false);
                selectedCycleId = null;
            }
            loadCycles();
        }));
    }

    @FXML
    public void goHome() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml", "Agri-Hub — Dashboard");
    }
}
