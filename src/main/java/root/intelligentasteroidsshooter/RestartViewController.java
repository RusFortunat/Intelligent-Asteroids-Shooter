package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class RestartViewController {
    @FXML
    private Label scoreText;

    @FXML
    private Label question;

    public Button yesButton;
    public Button closeButton;

    @FXML
    protected void setLabels(int score){
        this.scoreText.setText("Final score: " + score);
    }

    @FXML
    protected void onYesButtonClick() throws Exception {
        Stage stage = (Stage) yesButton.getScene().getWindow();
        stage.close();
        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        singlePlayerView.start(singlePlayer);
    }

    @FXML
    protected void onCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

}
