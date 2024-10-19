module root.intelligentasteroidsshooter {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires jdk.security.jgss;

    opens root.intelligentasteroidsshooter to javafx.fxml;
    exports root.intelligentasteroidsshooter;
}