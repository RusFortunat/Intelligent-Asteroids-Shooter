package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AITrainingPaneController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Pane gamingPane;
    @FXML
    private Pane graphPane;
    @FXML
    private Button beginTraining;
    @FXML
    private Button closeWindow;
    //@FXML
    //private Button nextButton; // for future work

    @FXML
    protected void beginToTrainAI(){

        Stage stage = (Stage) beginTraining.getScene().getWindow();
        EvolutionarySearch ourBestAICoach = new EvolutionarySearch();
        ourBestAICoach.start(stage, anchorPane, gamingPane, graphPane);
    }

    @FXML
    protected void closeTrainingSession(){
        Stage stage = (Stage) closeWindow.getScene().getWindow();
        stage.close();
    }

    public void setBackground(){
        // main background
        Image anchorBackgrFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\trainAIbackground.png"); // doesn't render without full path
        BackgroundImage anchorBI= new BackgroundImage(anchorBackgrFile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background anchorPaneBackgr = new Background(anchorBI);
        anchorPane.setBackground(anchorPaneBackgr);

        // gaming pane background
        Image gamingWindowBackgrFile = new Image("C:\\Users\\mrusl\\Desktop\\Java Projects\\Intelligent-Asteroids-Shooter" +
                "\\src\\main\\resources\\root\\intelligentasteroidsshooter\\space2.gif"); // doesn't render without full path
        BackgroundImage gamingBI= new BackgroundImage(gamingWindowBackgrFile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background gamingPaneBackgr = new Background(gamingBI);
        gamingPane.setBackground(gamingPaneBackgr);
        Rectangle clip = new Rectangle(EvolutionarySearch.GameWindowWIDTH,EvolutionarySearch.GameWindowHEIGHT);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        gamingPane.setClip(clip);

        // graph pane background
        //graphPane.setPrefSize(400, 400);
        graphPane.setBackground(new Background(new BackgroundFill(Color.GAINSBORO, null, null)));
    }
}
