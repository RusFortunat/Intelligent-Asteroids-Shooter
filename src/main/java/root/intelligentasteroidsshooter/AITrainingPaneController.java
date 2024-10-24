package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
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
}
