package com.rubaet.agrihub.ui.ai;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the AI Assistant Hub screen.
 * Supports General Chat, Crop Planner, and Disease Identifier modes.
 */
public class AskAi implements Initializable {

    @FXML private TextArea chatHistory;
    @FXML private TextField userInput;
    @FXML private Button sendButton;
    @FXML private javafx.scene.control.ComboBox<String> modeSelector;

    /**
     * Conversation memory — each entry is a {role, content} map.
     * Capped at MAX_HISTORY entries (20 turns = 10 exchanges) to stay within token limits.
     */
    private final List<Map<String, String>> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 20;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        modeSelector.getItems().addAll("🤖 General Chat", "🌱 Crop Planner", "🔬 Disease Identifier");
        modeSelector.setValue("🤖 General Chat");
        modeSelector.setOnAction(e -> updatePromptText());

        chatHistory.appendText("AgriBot: Hello! I'm AgriBot, your Agricultural AI Expert. 🌿\n\n");
        chatHistory.appendText("I can help you with:\n");
        chatHistory.appendText("  • Crop selection and planning\n");
        chatHistory.appendText("  • Disease identification and treatment\n");
        chatHistory.appendText("  • Soil management and fertilizers\n");
        chatHistory.appendText("  • Weather impact on farming\n");
        chatHistory.appendText("  • Intercropping and yield optimization\n\n");
        chatHistory.appendText("What would you like to know?\n\n");
        chatHistory.appendText("─".repeat(50) + "\n\n");
    }

    private void updatePromptText() {
        String mode = modeSelector.getValue();
        if (mode.contains("Crop")) {
            userInput.setPromptText("E.g. Season: Summer, Soil: Sandy...");
        } else if (mode.contains("Disease")) {
            userInput.setPromptText("Describe the plant symptoms...");
        } else {
            userInput.setPromptText("Ask AgriBot anything about farming...");
        }
    }

    @FXML
    public void sendMessage(ActionEvent event) {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;

        String mode = modeSelector.getValue();

        // Append user message to chat display
        chatHistory.appendText("You (" + mode + "): " + message + "\n\n");
        userInput.clear();

        // Show loading state
        sendButton.setDisable(true);
        sendButton.setText("⏳ Thinking...");
        chatHistory.appendText("AgriBot: ⏳ Thinking...\n\n");

        java.util.concurrent.CompletableFuture<String> aiFuture;
        if (mode.contains("Crop")) {
            aiFuture = com.rubaet.agrihub.service.ApiService.askAiCropPlan(message, "Custom Input");
        } else if (mode.contains("Disease")) {
            aiFuture = com.rubaet.agrihub.service.ApiService.askAiDisease(message);
        } else {
            aiFuture = com.rubaet.agrihub.service.ApiService.askAi(message, new ArrayList<>(conversationHistory));
        }

        aiFuture.thenAccept(jsonData -> {
                Platform.runLater(() -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(jsonData);

                        String reply;
                        if (root.has("candidates")) {
                            reply = root.path("candidates").get(0)
                                       .path("content").path("parts").get(0)
                                       .path("text").asText();
                            reply = reply.replaceAll("\\*\\*", "").replaceAll("\\*", "");
                        } else if (root.has("title")) {
                            reply = "[Server Error: " + root.get("detail").asText() + "]";
                        } else {
                            reply = "[Unexpected response format]";
                        }

                        // Replace the thinking text with the actual reply
                        String currentText = chatHistory.getText();
                        chatHistory.setText(currentText.replace("AgriBot: ⏳ Thinking...\n\n", "AgriBot: " + reply + "\n\n"));
                        chatHistory.appendText("─".repeat(50) + "\n\n");

                        // Add this exchange to conversation memory
                        addToHistory("user", message);
                        addToHistory("model", reply);

                    } catch (Exception e) {
                        String currentText = chatHistory.getText();
                        chatHistory.setText(currentText.replace("AgriBot: ⏳ Thinking...\n\n", "AgriBot: [Error parsing AI response]\n\n"));
                    } finally {
                        restoreSendButton();
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    String currentText = chatHistory.getText();
                    chatHistory.setText(currentText.replace("AgriBot: ⏳ Thinking...\n\n", "AgriBot: [Failed to connect to backend — is the server running?]\n\n"));
                    restoreSendButton();
                });
                return null;
            });
    }

    /** Adds a message to history and trims to MAX_HISTORY entries. */
    private void addToHistory(String role, String content) {
        Map<String, String> entry = new HashMap<>();
        entry.put("role", role);
        entry.put("content", content);
        conversationHistory.add(entry);
        while (conversationHistory.size() > MAX_HISTORY) {
            conversationHistory.remove(0);
        }
    }

    private void restoreSendButton() {
        sendButton.setDisable(false);
        sendButton.setText("Send ▶");
    }

    /** Quick action: pre-fills the input with a common farming question */
    @FXML
    public void quickAsk(ActionEvent event) {
        Button btn = (Button) event.getSource();
        userInput.setText(btn.getUserData().toString());
        userInput.requestFocus();
        userInput.positionCaret(userInput.getText().length());
    }

    /** Back button — navigates to the main dashboard with fade transition */
    @FXML
    public void goHome() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml",
            "Agri-Hub — Dashboard"
        );
    }
}
