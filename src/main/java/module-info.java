module root.intelligentasteroidsshooter {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires jdk.security.jgss;
    requires java.desktop;
    requires jdk.compiler;

    opens root.intelligentasteroidsshooter to javafx.fxml;
    exports root.intelligentasteroidsshooter;
    exports root.intelligentasteroidsshooter.model;
    opens root.intelligentasteroidsshooter.model to javafx.fxml;
    exports root.intelligentasteroidsshooter.singlePlayer;
    opens root.intelligentasteroidsshooter.singlePlayer to javafx.fxml;
    exports root.intelligentasteroidsshooter.trainAI;
    opens root.intelligentasteroidsshooter.trainAI to javafx.fxml;
}