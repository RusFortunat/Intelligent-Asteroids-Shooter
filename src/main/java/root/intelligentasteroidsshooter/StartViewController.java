package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import root.intelligentasteroidsshooter.singlePlayer.ChooseFighterController;
import root.intelligentasteroidsshooter.trainAI.AITrainingPaneController;

import java.io.IOException;

public class StartViewController {
    @FXML
    private Button singlePlayerButton;
    @FXML
    private Button trainAIButton;
    @FXML
    private ChoiceBox languageChoice;
    @FXML
    private Pane startViewPane;
    @FXML
    private Label chooseMode;
    @FXML
    private Label language;

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
    protected void onTrainAIButtonClick() throws IOException {
        FXMLLoader AITrainingView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("AI-training-pane.fxml"));
        Scene AITrainingScene = new Scene(AITrainingView.load());
        AITrainingPaneController aiTrainingViewController = AITrainingView.getController();
        aiTrainingViewController.setBackground();

        Stage AITrainingStage = new Stage();
        AITrainingStage.setScene(AITrainingScene);
        AITrainingStage.show();
        Stage stage = (Stage) trainAIButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void setBackground() throws IOException{
        Image backgroundFile =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/stars_moving.gif").toString());
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background paneBackgr = new Background(myBI);
        startViewPane.setBackground(paneBackgr);
        language.setVisible(false);
        languageChoice.setVisible(false);
        chooseMode.setTextFill(Color.WHITE);
    }

    @FXML
    protected void setChooseModeLabel() {chooseMode.setTextFill(Color.WHITE);}

    @FXML
    protected void setLangugeLabel() {language.setTextFill(Color.WHITE);}
}
