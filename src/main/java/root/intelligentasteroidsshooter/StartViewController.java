package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class StartViewController {
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
