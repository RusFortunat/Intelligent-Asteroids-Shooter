package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SinglePlayerController {
    @FXML
    private Label bauarbeitenText;

    SinglePlayerView game = new SinglePlayerView();

    @FXML
    protected void onSinglePlayerButtonClick() {
        Pane pane = new Pane();
        pane.getChildren().addAll(game.getView());
        Scene singleGameScene = new Scene(pane);
        Stage singleGameWindow = new Stage();
        singleGameWindow.setScene(singleGameScene);
        singleGameWindow.show();
    }

    @FXML
    protected void onTrainAIButtonClick() {
        bauarbeitenText.setText("Bauarbeiten...");
    }
}
