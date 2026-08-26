package com.rubaet.agrihub;

import com.rubaet.agrihub.state.AppState;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class Functions implements Initializable {

    @FXML private Label userEmailLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String email = AppState.getInstance().getUserEmail();
        if (userEmailLabel != null) {
            userEmailLabel.setText(email.isEmpty() ? "Guest" : email);
        }
    }

    @FXML public void weatherBtn(ActionEvent e)      { navigate(e, "Weather.fxml", "Weather Dashboard"); }
    @FXML public void askAiBtn(ActionEvent e)         { navigate(e, "AskAi.fxml", "AI Assistant Hub"); }
    @FXML public void farmProfileBtn(ActionEvent e)   { navigate(e, "FarmProfile.fxml", "My Farm Profile"); }
    @FXML public void analyticsBtn(ActionEvent e)     { navigate(e, "Analytics.fxml", "Yield Analytics"); }
    @FXML public void cropDiaryBtn(ActionEvent e)     { navigate(e, "CropDiary.fxml", "Crop Diary"); }
    @FXML public void alertsBtn(ActionEvent e)        { navigate(e, "Alerts.fxml", "Smart Alerts"); }
    @FXML public void yieldHistoryBtn(ActionEvent e)  { navigate(e, "YieldHistory.fxml", "Yield History"); }
    @FXML public void exportReportBtn(ActionEvent e)  { navigate(e, "ExportReport.fxml", "Export Report"); }

    private void navigate(ActionEvent event, String fxml, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneTransition.navigateTo(stage, "/com/rubaet/agrihub/views/" + fxml, "Agri-Hub — " + title);
    }
}
