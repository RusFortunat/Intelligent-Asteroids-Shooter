package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ChooseFighterController {
    @FXML
    private Button pepeButton;
    @FXML
    private Button SWButton;
    @FXML
    private Button STButton;
    @FXML
    private AnchorPane chooseFighterPane;
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

    private String chosenImage;

    @FXML
    protected void onSTButtonClick() throws Exception {
        // if I don't provide absolute path, images don't render
        chosenImage = "C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\start-treck-removebg-preview.png";
        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        try{
            singlePlayerView.start(singlePlayer, chosenImage);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        Stage stage = (Stage) STButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onSWButtonClick() throws Exception {
        // if I don't provide absolute path, images don't render
        chosenImage = "C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter\\src\\main" +
                "\\resources\\root\\intelligentasteroidsshooter\\falcon_no_bgr.png";
        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        try{
            singlePlayerView.start(singlePlayer, chosenImage);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        Stage stage = (Stage) SWButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onPepeButtonClick() throws Exception {
        // if I don't provide absolute path, images don't render
        chosenImage = "C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter\\src\\main" +
                "\\resources\\root\\intelligentasteroidsshooter\\pepeShip_nobackgr.png";
        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        try{
            singlePlayerView.start(singlePlayer, chosenImage);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        Stage stage = (Stage) pepeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void setBackground(){
        Image backgroundFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\deep_space.jpg"); // doesn't render without full path
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        //BackgroundFill myBF = new BackgroundFill(Color.BLUEVIOLET, new CornerRadii(1),
        //        new Insets(0.0,0.0,0.0,0.0));// or null for the padding
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
    }
}
