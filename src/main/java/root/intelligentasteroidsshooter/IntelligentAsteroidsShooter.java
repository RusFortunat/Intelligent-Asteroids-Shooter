package root.intelligentasteroidsshooter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;

import java.io.IOException;

public class IntelligentAsteroidsShooter extends Application  {

    @Override
    public void start(Stage window) throws IOException {
        FXMLLoader startView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("start-view.fxml"));

        Scene startScene = new Scene(startView.load());

        window.setTitle("Our asteroids shooter!");
        window.setScene(startScene);
        window.show();
    }

    public static void main(String[] args) throws SQLException{
        launch();
    }
}