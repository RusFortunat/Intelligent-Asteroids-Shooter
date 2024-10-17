module root.intelligentasteroidsshooter {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens root.intelligentasteroidsshooter to javafx.fxml;
    exports root.intelligentasteroidsshooter;
}