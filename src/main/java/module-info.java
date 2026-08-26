module com.rubaet.agrihub {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;

    requires com.fasterxml.jackson.databind;
    requires atlantafx.base;
    requires java.net.http;

    opens com.rubaet.agrihub to javafx.fxml;
    opens com.rubaet.agrihub.ui.weather to javafx.fxml;
    opens com.rubaet.agrihub.ui.auth to javafx.fxml;
    opens com.rubaet.agrihub.ui.profile to javafx.fxml;
    opens com.rubaet.agrihub.ui.analytics to javafx.fxml;
    opens com.rubaet.agrihub.ui.ai to javafx.fxml;
    opens com.rubaet.agrihub.ui.diary to javafx.fxml;
    opens com.rubaet.agrihub.ui.alerts to javafx.fxml;
    opens com.rubaet.agrihub.ui.history to javafx.fxml;
    opens com.rubaet.agrihub.ui.report to javafx.fxml;

    exports com.rubaet.agrihub;
    exports com.rubaet.agrihub.ui.weather;
    exports com.rubaet.agrihub.ui.auth;
    exports com.rubaet.agrihub.ui.profile;
    exports com.rubaet.agrihub.ui.analytics;
    exports com.rubaet.agrihub.ui.ai;
    exports com.rubaet.agrihub.ui.diary;
    exports com.rubaet.agrihub.ui.alerts;
    exports com.rubaet.agrihub.ui.history;
    exports com.rubaet.agrihub.ui.report;
}