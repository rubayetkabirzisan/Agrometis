package com.rubaet.agrihub.ui.report;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.state.AppState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ExportReportController implements Initializable {

    @FXML private Button exportBtn;
    @FXML private Button previewBtn;
    @FXML private Label statusLabel;
    @FXML private TextArea previewArea;

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        previewArea.setVisible(false);
        previewArea.setManaged(false);
    }

    @FXML
    public void previewReport(ActionEvent event) {
        previewBtn.setDisable(true);
        statusLabel.setText("⏳ Generating preview...");

        fetchReport().thenAccept(bytes -> {
            Platform.runLater(() -> {
                previewBtn.setDisable(false);
                statusLabel.setText("✅ Preview loaded!");
                previewArea.setVisible(true);
                previewArea.setManaged(true);
                previewArea.setText(new String(bytes));
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                previewBtn.setDisable(false);
                statusLabel.setText("❌ Failed: " + e.getMessage());
            });
            return null;
        });
    }

    @FXML
    public void exportReport(ActionEvent event) {
        exportBtn.setDisable(true);
        statusLabel.setText("⏳ Generating report...");

        fetchReport().thenAccept(bytes -> {
            Platform.runLater(() -> {
                exportBtn.setDisable(false);

                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save Farm Report");
                chooser.setInitialFileName("AgriHub_Report_" + LocalDate.now() + ".txt");
                chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"));

                File file = chooser.showSaveDialog(AppState.getInstance().getPrimaryStage());
                if (file != null) {
                    try {
                        Files.write(file.toPath(), bytes);
                        statusLabel.setText("✅ Report saved to: " + file.getName());
                    } catch (Exception e) {
                        statusLabel.setText("❌ Failed to save: " + e.getMessage());
                    }
                } else {
                    statusLabel.setText("Export cancelled.");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                exportBtn.setDisable(false);
                statusLabel.setText("❌ Failed to generate report.");
            });
            return null;
        });
    }

    private java.util.concurrent.CompletableFuture<byte[]> fetchReport() {
        String apiKey = System.getenv("AGRIHUB_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("agrihub.api.key", "agri_hub_desktop_client_secret_2026");
        }

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reports/farm-summary"))
                .header("X-API-KEY", apiKey)
                .GET();

        String token = AppState.getInstance().getJwtToken();
        if (token != null && !token.isEmpty()) {
            rb.header("Authorization", "Bearer " + token);
        }

        return CLIENT.sendAsync(rb.build(), HttpResponse.BodyHandlers.ofByteArray())
                      .thenApply(HttpResponse::body);
    }

    @FXML
    public void goHome() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml", "Agri-Hub — Dashboard");
    }
}
