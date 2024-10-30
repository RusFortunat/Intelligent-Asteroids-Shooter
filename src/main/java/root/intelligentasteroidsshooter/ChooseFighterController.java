package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChooseFighterController {
    @FXML
    private AnchorPane chooseFighterPane;
    @FXML
    private VBox diffcultySelection;
    @FXML
    private Button pepeButton;
    @FXML
    private Button SWButton;
    @FXML
    private Button STButton;
    @FXML
    private Button easy;
    @FXML
    private Button normal;
    @FXML
    private Button hard;
    @FXML
    private Label chooseFighterText;
    @FXML
    private Label controls;
    @FXML
    private Label up;
    @FXML
    private Label down;
    @FXML
    private Label left;
    @FXML
    private Label right;
    @FXML
    private Label space;
    @FXML
    private Label difficultyLabel;

    private String chosenImage;
    private String chosenDifficulty;

    @FXML
    protected void onSTButtonClick() throws Exception {
        chosenImage = "/root/intelligentasteroidsshooter/images/start-treck-removebg-preview.png";
        diffcultySelection.setVisible(true);
    }

    @FXML
    protected void onSWButtonClick() throws Exception {
        chosenImage = "/root/intelligentasteroidsshooter/images/falcon_no_bgr.png";
        diffcultySelection.setVisible(true);
    }

    @FXML
    protected void onPepeButtonClick() throws Exception {
        chosenImage = "/root/intelligentasteroidsshooter/images/pepeShip_nobackgr.png";
        diffcultySelection.setVisible(true);
    }

    @FXML
    protected void onEasyClick(){
        chosenDifficulty = easy.getText();
        //System.out.println("chosenDifficulty: " + chosenDifficulty);

        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        try{
            singlePlayerView.start(singlePlayer, chosenImage, chosenDifficulty);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }

        Stage stage = (Stage) pepeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onNormalClick(){
        chosenDifficulty = normal.getText();

        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        try{
            singlePlayerView.start(singlePlayer, chosenImage, chosenDifficulty);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }

        Stage stage = (Stage) pepeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onHardClick(){
        chosenDifficulty = hard.getText();

        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        try{
            singlePlayerView.start(singlePlayer, chosenImage, chosenDifficulty);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }

        Stage stage = (Stage) pepeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void setBackground(){
        Image backgroundFile =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/deep_space.jpg").toString());
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background paneBackgr = new Background(myBI);
        chooseFighterPane.setBackground(paneBackgr);
        // there must be a way to set the textFill for all children simultaneously
        chooseFighterText.setTextFill(Color.WHITE);
        controls.setTextFill(Color.WHITE);
        up.setTextFill(Color.WHITE);
        down.setTextFill(Color.WHITE);
        left.setTextFill(Color.WHITE);
        right.setTextFill(Color.WHITE);
        space.setTextFill(Color.WHITE);

        diffcultySelection.setBackground(new Background(new BackgroundFill(Color.GAINSBORO, null, null)));
        diffcultySelection.setVisible(false);
        diffcultySelection.setAlignment(Pos.CENTER);
        diffcultySelection.setSpacing(10);
        diffcultySelection.setPadding(new Insets(10));
        difficultyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    }
}
