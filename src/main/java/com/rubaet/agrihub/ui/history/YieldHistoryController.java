package com.rubaet.agrihub.ui.history;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.service.ApiService;
import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Year;
import java.util.*;
import java.util.ResourceBundle;

public class YieldHistoryController implements Initializable {

    @FXML private TextField cropField;
    @FXML private TextField seasonField;
    @FXML private TextField yearField;
    @FXML private TextField yieldField;
    @FXML private TextField revenueField;
    @FXML private TextArea notesField;
    @FXML private Button recordBtn;
    @FXML private Label statusLabel;

    @FXML private LineChart<String, Number> yieldChart;
    @FXML private VBox recordListBox;
    @FXML private Label summaryLabel;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        yearField.setText(String.valueOf(Year.now().getValue()));
        loadRecords();
    }

    @FXML
    public void recordHarvest(ActionEvent event) {
        String crop = cropField.getText().trim();
        if (crop.isEmpty()) { statusLabel.setText("⚠ Crop name is required."); return; }

        int year;
        double yieldKg;
        double revenue = 0;
        try {
            year = Integer.parseInt(yearField.getText().trim());
            yieldKg = Double.parseDouble(yieldField.getText().trim());
            if (!revenueField.getText().trim().isEmpty()) {
                revenue = Double.parseDouble(revenueField.getText().trim());
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠ Please enter valid numbers.");
            return;
        }

        recordBtn.setDisable(true);
        statusLabel.setText("Recording...");

        ApiService.recordHarvest(crop, seasonField.getText().trim(), year, yieldKg, revenue, notesField.getText().trim())
            .thenAccept(body -> Platform.runLater(() -> {
                recordBtn.setDisable(false);
                statusLabel.setText("✅ Harvest recorded!");
                cropField.clear();
                seasonField.clear();
                yieldField.clear();
                revenueField.clear();
                notesField.clear();
                loadRecords();
            }))
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    recordBtn.setDisable(false);
                    statusLabel.setText("❌ Failed: " + e.getMessage());
                });
                return null;
            });
    }

    private void loadRecords() {
        // Load list of records
        ApiService.getHarvestRecords().thenAccept(body -> {
            Platform.runLater(() -> {
                recordListBox.getChildren().clear();
                try {
                    JsonNode arr = MAPPER.readTree(body);
                    if (arr.isArray()) {
                        for (JsonNode r : arr) {
                            Label row = new Label(String.format("  %s  |  %s %d  |  %,.0f kg  |  $%,.2f",
                                r.path("cropName").asText(),
                                r.path("season").asText("—"),
                                r.path("year").asInt(),
                                r.path("actualYieldKg").asDouble(),
                                r.path("revenueUsd").asDouble()
                            ));
                            row.getStyleClass().add("text-body");
                            row.setStyle("-fx-padding: 8 0; -fx-border-color: transparent transparent #30363d transparent; -fx-border-width: 0 0 1 0;");
                            recordListBox.getChildren().add(row);
                        }
                    }
                    if (recordListBox.getChildren().isEmpty()) {
                        Label empty = new Label("No harvest records yet.");
                        empty.getStyleClass().add("text-muted");
                        recordListBox.getChildren().add(empty);
                    }
                } catch (Exception ignored) {}
            });
        });

        // Load summary for chart
        ApiService.getHarvestSummary().thenAccept(body -> {
            Platform.runLater(() -> {
                try {
                    JsonNode root = MAPPER.readTree(body);

                    // Update chart
                    yieldChart.getData().clear();
                    JsonNode yieldByYear = root.path("yieldByYear");
                    if (yieldByYear.isObject() && yieldByYear.size() > 0) {
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName("Total Yield (kg)");

                        TreeMap<String, Double> sorted = new TreeMap<>();
                        yieldByYear.fields().forEachRemaining(e ->
                            sorted.put(e.getKey(), e.getValue().asDouble()));

                        sorted.forEach((yr, val) -> series.getData().add(new XYChart.Data<>(yr, val)));
                        yieldChart.getData().add(series);

                        // Style the line green
                        Platform.runLater(() -> {
                            if (series.getNode() != null) {
                                series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: #22c55e; -fx-stroke-width: 2px;");
                            }
                        });
                    }

                    // Summary text
                    int total = root.path("totalRecords").asInt(0);
                    summaryLabel.setText(total + " harvest records on file");

                } catch (Exception ignored) {}
            });
        });
    }

    @FXML
    public void goHome() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml", "Agri-Hub — Dashboard");
    }
}
