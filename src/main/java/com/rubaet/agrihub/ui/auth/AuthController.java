package com.rubaet.agrihub.ui.auth;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.service.ApiService;
import com.rubaet.agrihub.state.AppState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class AuthController {

    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Button loginBtn;

    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private Button regBtn;

    @FXML private Label errorLabel;

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("⏳ Signing in...");
        errorLabel.setText("");

        ApiService.login(email, password).thenAccept(result -> {
            Platform.runLater(() -> {
                if (result != null && result[0] != null && !result[0].isEmpty()) {
                    AppState.getInstance().setJwtToken(result[0]);
                    if (result[1] != null) AppState.getInstance().setRefreshToken(result[1]);
                    AppState.getInstance().setUserEmail(email);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    SceneTransition.navigateTo(stage, "/com/rubaet/agrihub/Functions.fxml", "Agri-Hub — Dashboard");
                } else {
                    showError("Invalid email or password.");
                    resetLoginBtn();
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showError("Cannot connect to server. Is the backend running?");
                resetLoginBtn();
            });
            return null;
        });
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        regBtn.setDisable(true);
        regBtn.setText("⏳ Creating account...");
        errorLabel.setText("");

        ApiService.register(email, password).thenAccept(result -> {
            Platform.runLater(() -> {
                if (result != null && result[0] != null && !result[0].isEmpty()) {
                    AppState.getInstance().setJwtToken(result[0]);
                    if (result[1] != null) AppState.getInstance().setRefreshToken(result[1]);
                    AppState.getInstance().setUserEmail(email);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    SceneTransition.navigateTo(stage, "/com/rubaet/agrihub/Functions.fxml", "Agri-Hub — Dashboard");
                } else {
                    showError("Registration failed. Email might already be in use.");
                    resetRegBtn();
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showError("Connection error during registration.");
                resetRegBtn();
            });
            return null;
        });
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
    }
    private void resetLoginBtn() { loginBtn.setDisable(false); loginBtn.setText("Sign In"); }
    private void resetRegBtn()  { regBtn.setDisable(false); regBtn.setText("Create Account"); }
}
