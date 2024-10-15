module root.mac_version_of_api {
    requires javafx.controls;
    requires javafx.fxml;


    opens root.mac_version_of_api to javafx.fxml;
    exports root.mac_version_of_api;
}