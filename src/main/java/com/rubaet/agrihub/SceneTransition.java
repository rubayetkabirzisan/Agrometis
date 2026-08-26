package com.rubaet.agrihub;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Reusable utility for animated scene navigation.
 * Every screen transition does: fade-out → swap scene → fade-in.
 */
public class SceneTransition {

    private static final double FADE_DURATION_MS = 200;

    /**
     * Navigate to the given FXML path with a smooth fade transition.
     * @param stage     The stage to navigate on.
     * @param fxmlPath  Classpath resource path (e.g. "/com/rubaet/agrihub/Functions.fxml").
     * @param title     Window title for the new scene.
     */
    public static void navigateTo(Stage stage, String fxmlPath, String title) {
        // Fade out current scene
        Node root = stage.getScene().getRoot();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION_MS), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(SceneTransition.class.getResource(fxmlPath));
                Scene newScene = new Scene(loader.load());
                newScene.getStylesheets().add(
                    Start.class.getResource("/com/rubaet/agrihub/styles/application.css").toExternalForm()
                );
                stage.setScene(newScene);
                stage.setTitle(title);

                // Fade in the new scene
                newScene.getRoot().setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MS), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        fadeOut.play();
    }

    /**
     * Overload that accepts an ActionEvent source node to resolve stage automatically.
     */
    public static void navigateTo(javafx.event.ActionEvent event, String fxmlPath, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        navigateTo(stage, fxmlPath, title);
    }
}
