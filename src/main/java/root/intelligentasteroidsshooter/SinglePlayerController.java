package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class SinglePlayerController {
    @FXML
    private Label bauarbeitenText;

    SinglePlayerView game = new SinglePlayerView();

    @FXML
    protected void onSinglePlayerButtonClick() throws IOException {
        Stage singlePlayer = new Stage();
        SinglePlayerView singlePlayerView = new SinglePlayerView();
        singlePlayerView.start(singlePlayer);
    }

    @FXML
    protected void onTrainAIButtonClick() {
        bauarbeitenText.setText("Bauarbeiten...");
    }
}
