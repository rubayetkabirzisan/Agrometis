package com.rubaet.agrihub;

import com.rubaet.agrihub.state.AppState;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import atlantafx.base.theme.PrimerDark;

import java.io.IOException;

public class Start extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // ── 1. Show Splash screen on a slim, undecorated stage ──
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader splashLoader = new FXMLLoader(Start.class.getResource("/com/rubaet/agrihub/Splash.fxml"));
        Scene splashScene = new Scene(splashLoader.load());
        splashScene.getStylesheets().add(
            Start.class.getResource("/com/rubaet/agrihub/styles/application.css").toExternalForm()
        );
        splashStage.setScene(splashScene);
        splashStage.setWidth(600);
        splashStage.setHeight(360);
        splashStage.centerOnScreen();
        splashStage.show();

        // ── 2. After 2.5 seconds, fade out splash and show main app ──
        PauseTransition pause = new PauseTransition(Duration.millis(2500));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), splashScene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                splashStage.close();
                try {
                    showAuthScreen(primaryStage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        });
        pause.play();
    }

    private void showAuthScreen(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Start.class.getResource("/com/rubaet/agrihub/Auth.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(
            Start.class.getResource("/com/rubaet/agrihub/styles/application.css").toExternalForm()
        );

        // Store stage globally — all controllers navigate through this
        AppState.getInstance().setPrimaryStage(stage);

        stage.setTitle("Agri-Hub — Agricultural Intelligence Hub");
        stage.setWidth(1200);
        stage.setHeight(760);
        stage.setMinWidth(980);
        stage.setMinHeight(640);

        // Fade in the auth scene
        scene.getRoot().setOpacity(0.0);
        stage.setScene(scene);
        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), scene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public static void main(String[] args) {
        launch();
    }
}
