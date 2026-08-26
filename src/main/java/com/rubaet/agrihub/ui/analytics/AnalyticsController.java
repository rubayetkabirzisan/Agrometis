package com.rubaet.agrihub.ui.analytics;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.service.ApiService;
import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML private TextField cropField;
    @FXML private TextField areaField;
    @FXML private ComboBox<String> seasonSelector;
    @FXML private Button estimateBtn;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.layout.VBox resultsPane;

    // Result labels
    @FXML private Label yieldLabel;
    @FXML private Label revenueLabel;
    @FXML private Label costLabel;
    @FXML private Label profitLabel;
    @FXML private Label marginLabel;

    // Charts
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart profitChart;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final double[] SEASON_FACTORS = {0.70, 1.0, 1.30};
    private static final String[] SEASON_LABELS  = {"🌧️ Poor Season (drought/flood)", "🌤️ Average Season", "☀️ Excellent Season"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seasonSelector.setItems(FXCollections.observableArrayList(SEASON_LABELS));
        seasonSelector.setValue(SEASON_LABELS[1]);
    }

    @FXML
    public void calculateEstimate(ActionEvent event) {
        String crop = cropField.getText().trim();
        String areaText = areaField.getText().trim();

        if (crop.isEmpty()) { errorLabel.setText("Please enter a crop name."); return; }
        if (areaText.isEmpty()) { errorLabel.setText("Please enter the area in hectares."); return; }

        double area;
        try {
            area = Double.parseDouble(areaText);
            if (area <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Area must be a positive number (e.g. 5.5).");
            return;
        }

        errorLabel.setText("");
        estimateBtn.setDisable(true);
        estimateBtn.setText("⏳ Calculating...");

        int selectedIndex = seasonSelector.getSelectionModel().getSelectedIndex();
        double factor = SEASON_FACTORS[Math.max(0, selectedIndex)];

        ApiService.getYieldEstimate(crop, area, factor).thenAccept(body -> {
            Platform.runLater(() -> {
                estimateBtn.setDisable(false);
                estimateBtn.setText("📊  Calculate Estimate");
                try {
                    JsonNode root = MAPPER.readTree(body);
                    if (root.has("error")) {
                        errorLabel.setText("Server error: " + root.get("error").asText());
                        return;
                    }

                    long yieldKg      = root.path("estimatedYieldKg").asLong();
                    double gross      = root.path("grossRevenueUSD").asDouble();
                    double cost       = root.path("estimatedCostUSD").asDouble();
                    double profit     = root.path("netProfitUSD").asDouble();
                    double margin     = root.path("profitMarginPercent").asDouble();

                    // Update stat labels
                    yieldLabel.setText(String.format("%,d kg", yieldKg));
                    revenueLabel.setText(String.format("$%,.2f", gross));
                    costLabel.setText(String.format("$%,.2f", cost));
                    profitLabel.setText(String.format("$%,.2f", profit));
                    marginLabel.setText(String.format("%.1f%%", margin));

                    // Show results pane
                    resultsPane.setVisible(true);
                    resultsPane.setManaged(true);

                    // Populate BarChart
                    updateBarChart(crop, gross, cost, profit);

                    // Populate PieChart
                    updatePieChart(cost, profit);

                } catch (Exception e) {
                    errorLabel.setText("Failed to parse server response.");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                estimateBtn.setDisable(false);
                estimateBtn.setText("📊  Calculate Estimate");
                errorLabel.setText("Connection failed. Is the backend running?");
            });
            return null;
        });
    }

    private void updateBarChart(String crop, double gross, double cost, double profit) {
        revenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(crop + " Estimate");
        series.getData().add(new XYChart.Data<>("Gross Revenue", gross));
        series.getData().add(new XYChart.Data<>("Estimated Cost", cost));
        series.getData().add(new XYChart.Data<>("Net Profit", profit));
        revenueChart.getData().add(series);

        // Style bars after they're added
        Platform.runLater(() -> {
            if (series.getData().size() >= 3) {
                series.getData().get(0).getNode().setStyle("-fx-bar-fill: #22c55e;");
                series.getData().get(1).getNode().setStyle("-fx-bar-fill: #f59e0b;");
                series.getData().get(2).getNode().setStyle("-fx-bar-fill: #0ea5e9;");
            }
        });
    }

    private void updatePieChart(double cost, double profit) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Est. Cost (35%)", cost),
            new PieChart.Data("Net Profit (65%)", profit)
        );
        profitChart.setData(pieData);

        // Style slices
        Platform.runLater(() -> {
            pieData.get(0).getNode().setStyle("-fx-pie-color: #f59e0b;");
            pieData.get(1).getNode().setStyle("-fx-pie-color: #22c55e;");
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
}
