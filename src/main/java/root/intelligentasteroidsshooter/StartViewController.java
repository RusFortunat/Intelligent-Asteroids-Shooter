package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class StartViewController {
    @FXML
    private Button singlePlayerButton;
    @FXML
    private Button trainAIButton;
    @FXML
    private Label bauarbeitenText;
    @FXML
    private Pane startViewPane;
    @FXML
    private Label chooseMode;
    @FXML
    private Label language;

    SinglePlayerView game = new SinglePlayerView();

    @FXML
    protected void onSinglePlayerButtonClick() throws IOException {
        FXMLLoader chooseFighterView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("choose-fighter.fxml"));
        Scene chooseFighterScene = new Scene(chooseFighterView.load());
        ChooseFighterController startViewController = chooseFighterView.getController();
        startViewController.setBackground();

        Stage chooseFighter = new Stage();
        chooseFighter.setScene(chooseFighterScene);
        chooseFighter.show();
        Stage stage = (Stage) singlePlayerButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onTrainAIButtonClick() {
        bauarbeitenText.setText("Bauarbeiten...");
    }

    @FXML
    protected void setBackground(){
        Image backgroundFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\stars_moving.gif"); // doesn't render without full path
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        //BackgroundFill myBF = new BackgroundFill(Color.BLUEVIOLET, new CornerRadii(1),
        //        new Insets(0.0,0.0,0.0,0.0));// or null for the padding
        Background paneBackgr = new Background(myBI);
        startViewPane.setBackground(paneBackgr);
    }

    @FXML
    protected void setChooseModeLabel() {chooseMode.setTextFill(Color.WHITE);}

    @FXML
    protected void setLangugeLabel() {language.setTextFill(Color.WHITE);}

    /*@FXML
    protected void setButtonsFont() {
        singlePlayerButton.setStyle("-fx-font-size:18");
        trainAIButton.setStyle("-fx-font-size:18");
    }*/
}
