module root.intelligentasteroidsshooter {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens root.intelligentasteroidsshooter to javafx.fxml;
    exports root.intelligentasteroidsshooter;
}