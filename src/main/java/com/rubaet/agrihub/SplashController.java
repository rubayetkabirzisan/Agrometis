package com.rubaet.agrihub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private ProgressBar loadingBar;
    @FXML private Label statusLabel;

    private static final String[] STATUSES = {
        "Initializing core modules...",
        "Connecting to AgriBot AI...",
        "Loading farm data...",
        "Preparing your dashboard..."
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Animate progress bar from 0 → 1 over 2 seconds
        Timeline timeline = new Timeline();
        int steps = 40;
        double stepDuration = 2000.0 / steps;

        for (int i = 0; i <= steps; i++) {
            final double progress = i / (double) steps;
            final int statusIdx = Math.min((int)(progress * STATUSES.length), STATUSES.length - 1);

            KeyFrame kf = new KeyFrame(Duration.millis(i * stepDuration), e -> {
                loadingBar.setProgress(progress);
                statusLabel.setText(STATUSES[statusIdx]);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }
}
