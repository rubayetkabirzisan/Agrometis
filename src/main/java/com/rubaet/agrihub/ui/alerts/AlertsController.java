package com.rubaet.agrihub.ui.alerts;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AlertsController implements Initializable {

    @FXML private VBox alertListBox;
    @FXML private Label unreadBadge;
    @FXML private Label statusLabel;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadAlerts();
    }

    private void loadAlerts() {
        ApiService.getAlerts().thenAccept(body -> {
            Platform.runLater(() -> {
                alertListBox.getChildren().clear();
                try {
                    JsonNode root = MAPPER.readTree(body);
                    long unread = root.path("unreadCount").asLong(0);
                    unreadBadge.setText(unread > 0 ? unread + " unread" : "All read ✓");

                    JsonNode alerts = root.path("alerts");
                    if (alerts.isArray() && alerts.size() > 0) {
                        for (JsonNode a : alerts) {
                            alertListBox.getChildren().add(buildAlertCard(a));
                        }
                    } else {
                        Label empty = new Label("🔕  No alerts yet. Your farm is all clear!");
                        empty.getStyleClass().add("text-muted");
                        empty.setStyle("-fx-font-size: 15px; -fx-padding: 40 0 0 0;");
                        alertListBox.getChildren().add(empty);
                    }
                } catch (Exception e) {
                    Label err = new Label("❌ Failed to load alerts.");
                    err.getStyleClass().add("text-error");
                    alertListBox.getChildren().add(err);
                }
            });
        });
    }

    private VBox buildAlertCard(JsonNode a) {
        Long id = a.path("id").asLong();
        String type = a.path("type").asText("INFO");
        String message = a.path("message").asText();
        String severity = a.path("severity").asText("INFO");
        boolean isRead = a.path("read").asBoolean(false);
        String time = a.path("createdAt").asText("—");

        VBox card = new VBox(8);
        card.getStyleClass().add("farm-stat-card");
        card.setPadding(new Insets(14, 18, 14, 18));
        if (!isRead) {
            card.setStyle("-fx-border-color: #22c55e60; -fx-background-color: #1a3d2b30;");
        }

        // Header row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = switch (severity) {
            case "CRITICAL" -> "🔴";
            case "WARNING" -> "🟡";
            default -> "🟢";
        };

        Label iconLabel = new Label(icon + " " + type);
        iconLabel.getStyleClass().add("text-card-title");

        Label severityBadge = new Label(severity);
        severityBadge.getStyleClass().add(switch (severity) {
            case "CRITICAL" -> "badge-error";
            case "WARNING" -> "badge-warning";
            default -> "badge-info";
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(time.length() > 16 ? time.substring(0, 16) : time);
        timeLabel.getStyleClass().add("text-small");

        header.getChildren().addAll(iconLabel, severityBadge, spacer, timeLabel);

        // Message
        Label msgLabel = new Label(message);
        msgLabel.getStyleClass().add("text-body");
        msgLabel.setWrapText(true);

        // Actions
        HBox actions = new HBox(8);
        if (!isRead) {
            Button readBtn = new Button("✓ Mark Read");
            readBtn.getStyleClass().add("btn-ghost");
            readBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
            readBtn.setOnAction(e -> markAsRead(id));
            actions.getChildren().add(readBtn);
        } else {
            Label readLabel = new Label("✓ Read");
            readLabel.getStyleClass().add("text-small");
            actions.getChildren().add(readLabel);
        }

        card.getChildren().addAll(header, msgLabel, actions);
        return card;
    }

    private void markAsRead(Long id) {
        ApiService.markAlertRead(id).thenAccept(b -> Platform.runLater(this::loadAlerts));
    }

    @FXML
    public void generateTestAlert(ActionEvent event) {
        statusLabel.setText("Generating test alert...");
        ApiService.createTestAlert().thenAccept(body -> {
            Platform.runLater(() -> {
                statusLabel.setText("✅ Test alert created!");
                loadAlerts();
            });
        });
    }

    @FXML
    public void refreshAlerts(ActionEvent event) {
        statusLabel.setText("Refreshing...");
        loadAlerts();
        statusLabel.setText("");
    }

    @FXML
    public void goHome() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml", "Agri-Hub — Dashboard");
    }
}
