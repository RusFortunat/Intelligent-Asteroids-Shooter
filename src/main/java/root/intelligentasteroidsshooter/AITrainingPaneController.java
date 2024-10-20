package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AITrainingPaneController {
    @FXML
    private Pane gamingPane;
    @FXML
    private Pane graphPane;
    @FXML
    private Label textAboveGamingPane;
    @FXML
    private Label textAboveGraphPane;
    @FXML
    private Button beginTraining;
    @FXML
    private Button closeWindow;

    @FXML
    protected void setTextAboveGamingPane() {
        textAboveGamingPane.setText("The neural network controls the ship's actions"); //+
                //"\nIt receives the observation data as an input and selects the best action.");
    }

    @FXML
    protected void setTextAboveGraphPane(){
        textAboveGraphPane.setText("We update the neural network parameters with the\n " +
                "Evolutionary Algorithm and track the improvement");
    }

    @FXML
    protected void beginToTrainAI(){

    }

    @FXML
    protected void closeTrainingSession(){
        Stage stage = (Stage) closeWindow.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void setBackground(){

    }
}
