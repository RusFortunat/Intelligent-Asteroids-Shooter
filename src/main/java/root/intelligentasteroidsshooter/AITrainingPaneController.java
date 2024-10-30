package root.intelligentasteroidsshooter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;

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
    @FXML
    private Button done;
    @FXML
    private Button confirm;
    @FXML
    private Label selection;
    @FXML
    private VBox messageWindow;
    @FXML
    private VBox loadListPane;
    @FXML
    private TextField userInputNN;
    @FXML
    private TextField userInputEpNum;
    @FXML
    private Label loadListLabel;
    @FXML
    private ListView loadList;
    @FXML
    private HBox wrapSelection;
    @FXML
    private HBox loadButtons;

    private int NNPoolSize;
    private int epNumber;
    private EvolutionarySearch ourBestAICoach; // i define both here to be able to stop them from other button clicks
    private NNPLaysGameOnly ourBestNetworkPlays;
    private LineChart<Number, Number> lineChart;

    @FXML
    protected void beginToTrainAI(){
        // for restarting purposes
        gamingPane.getChildren().clear();
        messageWindow.getChildren().clear();
        userInputNN.clear();
        userInputEpNum.clear();
        // stop previous sessions
        if(ourBestAICoach != null){
            if(ourBestAICoach.getPlayer() != null) ourBestAICoach.getPlayer().stop();
            if(ourBestAICoach.getTrainer() != null) ourBestAICoach.getTrainer().stop();
        }
        ourBestAICoach = null;
        if(ourBestNetworkPlays != null){
            if(ourBestNetworkPlays.getPlayer() != null) ourBestNetworkPlays.getPlayer().stop();
        }
        ourBestNetworkPlays = null;
        Text chooseParams = new Text("Choose simulation parameters:");
        chooseParams.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        HBox numOfNetworks = new HBox();
        Label numOfNNText = new Label("Neural Networks population size: ");
        numOfNNText.setFont(Font.font("Arial", 14));
        userInputNN.setVisible(true);
        numOfNetworks.getChildren().addAll(numOfNNText, userInputNN);
        HBox numOfEpisodes = new HBox();
        Label numOfEpText = new Label("Number of training episodes: ");
        numOfEpText.setFont(Font.font("Arial", 14));
        userInputEpNum.setVisible(true);
        numOfEpisodes.getChildren().addAll(numOfEpText, userInputEpNum);

        done.setVisible(true);
        done.setFont(Font.font("Arial", 14));

        Text note = new Text("Setting pool size to 100 networks and total episodes to 10\n " +
                "should produce results within a minute, but they will be bad.\n" +
                "To get good networks, set pool size to about 5000-10000 and the number\n" +
                "of episodes to 100-300. It takes about two hours on my PC to finish training");
        note.setLineSpacing(5);
        note.setFont(Font.font("Arial", 12));

        messageWindow.getChildren().addAll(chooseParams, numOfNetworks, numOfEpisodes, done,note);
        messageWindow.setAlignment(Pos.CENTER);
        messageWindow.setSpacing(5);
        messageWindow.setVisible(true);
    }

    @FXML
    protected void passParamenters(){
        //System.out.println("Button clicked");
        // get networks pool size
        try{
            NNPoolSize = Integer.valueOf(userInputNN.getText());
            //System.out.println("NNPoolSize " + NNPoolSize);
        }catch(Exception e){
            userInputNN.setText("Invalid Input!"); // don't mess with me brother
        }
        // get total episodes
        try{
            epNumber = Integer.valueOf(userInputEpNum.getText());
            //System.out.println("epNumber " + epNumber);
        }catch(Exception e){
            userInputEpNum.setText("Invalid Input!");
        }

        // input check
        if(NNPoolSize > 0 && epNumber > 0){
            beginTraining.setText("Restart");
            messageWindow.setVisible(false);

            Stage stage = (Stage) beginTraining.getScene().getWindow();
            ourBestAICoach = new EvolutionarySearch();
            ourBestAICoach.start(gamingPane, graphPane, messageWindow, NNPoolSize, epNumber, lineChart);
        }
    }

    @FXML
    protected void loadTrainedModelButton(){
        // stop all processes if any are running
        if(ourBestAICoach != null){
            if(ourBestAICoach.getPlayer() != null) ourBestAICoach.getPlayer().stop();
            if(ourBestAICoach.getTrainer() != null) ourBestAICoach.getTrainer().stop();
        }
        ourBestAICoach = null;
        if(ourBestNetworkPlays != null){
            if(ourBestNetworkPlays.getPlayer() != null) ourBestNetworkPlays.getPlayer().stop();
        }
        ourBestNetworkPlays = null;
        messageWindow.setVisible(false);
        gamingPane.getChildren().clear();
        loadListPane.setVisible(true);
        loadList.getItems().clear();

        // load list of networks from our database
        StoreTrainedNNsDB NNDataBase = new StoreTrainedNNsDB("jdbc:h2:./trained-NNs-database");
        try{
            List<String> showAllSavedNNs = NNDataBase.getSavedList().stream()
                    .map(el->"Survival chance: " + Integer.valueOf(el)/50.0 + "%").toList(); // 5000 is the max run time
            //System.out.println("List of best Networks");
            //for(String entry:showAllSavedNNs) System.out.println(entry);
            loadList.getItems().addAll(showAllSavedNNs);
        }catch(SQLException s){}
        //System.out.println("showAllSavedNNs added to loadList");

        // react to cell click -> download selected network from database and pass it to a player
        loadList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue != null){
                    //System.out.println("ListView selection changed from oldValue = " + oldValue + " to newValue = " + newValue);
                    String[] parts = newValue.split(" ");
                    selection.setText("Your selection: " + parts[2]);
                }
            }
        });
        selection.setFont(Font.font("Arial", 18));
        //System.out.println("getSelectionModel() works");
    }

    @FXML
    protected void loadNetwork(){
        loadListPane.setVisible(false);
        String getSelectedScore = selection.getText();
        String[] parts = getSelectedScore.split(" ");
        String[] percent = parts[2].split("%");
        int score = (int) (Double.valueOf(percent[0])*50); // convert it back to the score that is stored in database
        System.out.println("score " + score);
        //System.out.println("score: " + score);
        try{
            StoreTrainedNNsDB NNDataBase = new StoreTrainedNNsDB("jdbc:h2:./trained-NNs-database");
            List<String> chosenNetwork = NNDataBase.toList(score); // get the desired network
            // list structure: score, firstLayerWeights, firstLayerBiases, secondLayerWeights, secondLayerBiases
            // HOWEVER, in SQL 0 index reserved for primary ID, and first column starts with 1, .get(0) and .get(1) give same result for me here
            String firstLayerWeightsCSV = chosenNetwork.get(2);
            String[] firstLayerWeightsStr = firstLayerWeightsCSV.split(",");
            Double[] firstLayerWeights = new Double[firstLayerWeightsStr.length];
            for(int entry = 0; entry < firstLayerWeightsStr.length; entry++){
                firstLayerWeights[entry] = Double.valueOf(firstLayerWeightsStr[entry]);
            }
            String firstLayerBiasesCSV = chosenNetwork.get(3);
            String[] firstLayerBiasesStr = firstLayerBiasesCSV.split(",");
            Double[] firstLayerBiases = new Double[firstLayerBiasesStr.length];
            for(int entry = 0; entry < firstLayerBiasesStr.length; entry++){
                firstLayerBiases[entry] = Double.valueOf(firstLayerBiasesStr[entry]);
            }
            String secondLayerWeightsCSV = chosenNetwork.get(4);
            String[] secondLayerWeightsStr = secondLayerWeightsCSV.split(",");
            Double[] secondLayerWeights = new Double[secondLayerWeightsStr.length];
            for(int entry = 0; entry < secondLayerWeightsStr.length; entry++){
                secondLayerWeights[entry] = Double.valueOf(secondLayerWeightsStr[entry]);
            }
            String secondLayerBiasesCSV = chosenNetwork.get(5);
            String[] secondLayerBiasesStr = secondLayerBiasesCSV.split(",");
            Double[] secondLayerBiases = new Double[secondLayerBiasesStr.length];
            for(int entry = 0; entry < secondLayerBiasesStr.length; entry++){
                secondLayerBiases[entry] = Double.valueOf(secondLayerBiasesStr[entry]);
            }

            int inputSize = firstLayerWeights.length/firstLayerBiases.length;
            NeuralNetwork loadedNetwork = new NeuralNetwork(inputSize, firstLayerBiases.length, secondLayerBiases.length);
            //System.out.println("loaded NN score: " + loadedNetwork.getScoreForPrinting());

            Stage stage = (Stage) confirm.getScene().getWindow();
            ourBestNetworkPlays = new NNPLaysGameOnly();
            ourBestNetworkPlays.start(gamingPane, graphPane, messageWindow, loadedNetwork, lineChart);
            //System.out.println("ourBestNetworkPlays.start() was reached");
        }catch(SQLException s){
            System.out.println(s.getMessage());
        }
    }

    @FXML
    protected void cancelLoad(){
        loadListPane.setVisible(false);
    }

    @FXML
    protected void closeTrainingSession(){
        Stage stage = (Stage) closeWindow.getScene().getWindow();
        stage.close();
    }

    public void setBackground(){
        // main background
        Image anchorBackgrFile =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/trainAIbackground.png").toString());
        BackgroundImage anchorBI= new BackgroundImage(anchorBackgrFile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background anchorPaneBackgr = new Background(anchorBI);
        anchorPane.setBackground(anchorPaneBackgr);
        Text welcomeMessage = new Text(50,20,"Welcome!");
        Text welcomeDescription = new Text("Here we will train neural networks \n        to control our spaceship.");
        Text startMessage = new Text("Press 'Begin' to start.");
        welcomeMessage.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeDescription.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        startMessage.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageWindow.getChildren().addAll(welcomeMessage, welcomeDescription, startMessage);
        messageWindow.setViewOrder(-100.0); // make sure it is always on top layer
        messageWindow.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));
        messageWindow.setSpacing(20);
        messageWindow.setPadding(new Insets(10));
        messageWindow.setAlignment(Pos.CENTER);
        messageWindow.getChildren().removeAll(done, userInputEpNum, userInputNN);
        //anchorPane.getChildren().add(messageWindow);

        // gaming pane background
        Image gamingWindowBackgrFile =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/space2.gif").toString());
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
        NumberAxis xAxis = new NumberAxis(0,20,5);
        NumberAxis yAxis = new NumberAxis(0, 5000,1000);
        xAxis.tickLabelFontProperty().set(Font.font(15));
        yAxis.tickLabelFontProperty().set(Font.font(15));
        xAxis.setLabel("Episode");
        yAxis.setLabel("Time ship stayed alive");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setStyle("-fx-font-size: " + 20 + "px;");
        Text graphLabel = new Text(70, 40, "Neural Networks Performance");
        graphLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        //XYChart.Series averScorePerEpisodeGraph = new XYChart.Series();
        lineChart.setLegendVisible(false);
        lineChart.setLayoutX(-15);
        lineChart.setLayoutY(50);
        //averScorePerEpisodeGraph.setName("average score"); // don't know how to manipulate legend and it looks super ugly
        //bestScorePerEdisodeGraph.setName("best performer");

        graphPane.getChildren().add(lineChart);
        graphPane.getChildren().add(graphLabel);

        // for loading stuff
        loadListPane.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));
        loadListLabel.setText("List of best Networks");
        loadListLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        loadListPane.setAlignment(Pos.CENTER);
        loadListPane.setPadding(new Insets(20));
        loadListPane.setSpacing(10);
        loadListPane.setVisible(false);
        //loadList.getStyleClass().add("customStyles.css");
        // nothing was working, so I used this solution: https://stackoverflow.com/a/51574405/24522071
        loadList.setCellFactory(stringListView -> new CenteredListViewCell());
        //loadList.setEditable(false);
        //loadList.setStyle("customStyles.css");
        loadList.setStyle("-fx-font-size:16.0; -fx-alignment: center;");
        wrapSelection.setAlignment(Pos.CENTER_LEFT);
        selection.setText("Your selection: ");
        selection.setFont(Font.font("Arial", 14));
        loadButtons.setSpacing(30);
        loadButtons.setAlignment(Pos.CENTER);
    }

    private void setNNPoolSizeValue(int value){
        NNPoolSize = value;
    }

    private void setEpNumber(int value){
        epNumber = value;
    }

    private TextField getUserInputNN(){ return userInputNN; }
    private TextField getUserInputEpNum(){ return userInputEpNum; }
    private VBox getMessageWindow(){ return messageWindow;}
}

final class CenteredListViewCell extends ListCell<String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            // Create the HBox
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);

            // Create centered Label
            Label label = new Label(item);
            label.setAlignment(Pos.CENTER);

            hBox.getChildren().add(label);
            setGraphic(hBox);
        }
    }
}