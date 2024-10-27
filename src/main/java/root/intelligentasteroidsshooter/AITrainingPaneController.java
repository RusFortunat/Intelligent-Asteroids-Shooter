package root.intelligentasteroidsshooter;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    private Button loadModel;
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

    private int NNPoolSize;
    private int epNumber;
    private EvolutionarySearch ourBestAICoach;
    private LineChart<Number, Number> lineChart;

    @FXML
    protected void beginToTrainAI(){
        graphPane.getChildren().clear();
        gamingPane.getChildren().clear();
        messageWindow.getChildren().clear();
        userInputNN.clear();
        userInputEpNum.clear();
        if(ourBestAICoach == null){
        }else{
            if(ourBestAICoach.getPlayer() == null){
            }else{
                ourBestAICoach.getPlayer().stop();
            }

            if(ourBestAICoach.getTrainer() == null){
            }else{
                ourBestAICoach.getTrainer().stop();
            }
        }
        ourBestAICoach = null;
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
            ourBestAICoach.start(stage, anchorPane, gamingPane, graphPane, messageWindow, NNPoolSize, epNumber, lineChart);
        }
    }

    @FXML
    protected void loadTrainedModelButton(){
        loadListPane.setVisible(true);

        // load list of networks from our database
        StoreTrainedNNsDB NNDataBase = new StoreTrainedNNsDB("jdbc:h2:./trained-NNs-database");
        try{
            List<String> showAllSavedNNs = NNDataBase.getSavedList();
            System.out.println("Best Networks list");
            for(String entry:showAllSavedNNs) System.out.println(entry);
            loadList.getItems().addAll(showAllSavedNNs);
        }catch(SQLException s){}

        // display ListView
        loadListLabel.setText("Best Neural Networks");
        loadListLabel.setFont(Font.font("Arial", 14));
        loadList.setCellFactory(new PropertyValueFactory<>("score"));
        loadList.getStyleClass().add("tableStyle.css");

        // react to cell click -> download selected network from database and pass it to a player
        loadList.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
    }

    // stolen from here: https://www.youtube.com/watch?v=Z7th7RSRitw
    private void selectionChanged(ObservableValue<? extends String> Observable, String oldVal, String newVal){
        ObservableList<String> selectedItems = loadList.getSelectionModel().getSelectedItems();
        String getSelectedItem = (selectedItems.isEmpty())?"No Selected Item":selectedItems.toString();
        selection.setText("Your selection: " + getSelectedItem);
        selection.setFont(Font.font("Arial", 14));
    }

    @FXML
    protected void loadNetwork(){
        String getSelectedScore = selection.getText();
        String[] parts = getSelectedScore.split(" ");
        int score = Integer.valueOf(parts[2]);
        StoreTrainedNNsDB NNDataBase = new StoreTrainedNNsDB("jdbc:h2:./trained-NNs-database");
        try{
            List<String> chosenNetwork = NNDataBase.toList(score); // get the desired network
            // list structure: score, firstLayerWeights, firstLayerBiases, secondLayerWeights, secondLayerBiases
            String firstLayerWeightsCSV = chosenNetwork.get(1);
            String[] firstLayerWeightsStr = firstLayerWeightsCSV.split(",");
            Double[] firstLayerWeights = new Double[firstLayerWeightsStr.length];
            for(int entry = 0; entry < firstLayerWeightsStr.length; entry++){
                firstLayerWeights[entry] = Double.valueOf(firstLayerWeightsStr[entry]);
            }
            String firstLayerBiasesCSV = chosenNetwork.get(1);
            String[] firstLayerBiasesStr = firstLayerBiasesCSV.split(",");
            Double[] firstLayerBiases = new Double[firstLayerBiasesStr.length];
            for(int entry = 0; entry < firstLayerBiasesStr.length; entry++){
                firstLayerBiases[entry] = Double.valueOf(firstLayerBiasesStr[entry]);
            }
            String secondLayerWeightsCSV = chosenNetwork.get(1);
            String[] secondLayerWeightsStr = secondLayerWeightsCSV.split(",");
            Double[] secondLayerWeights = new Double[secondLayerWeightsStr.length];
            for(int entry = 0; entry < secondLayerWeightsStr.length; entry++){
                secondLayerWeights[entry] = Double.valueOf(secondLayerWeightsStr[entry]);
            }
            String secondLayerBiasesCSV = chosenNetwork.get(1);
            String[] secondLayerBiasesStr = secondLayerBiasesCSV.split(",");
            Double[] secondLayerBiases = new Double[secondLayerBiasesStr.length];
            for(int entry = 0; entry < secondLayerBiasesStr.length; entry++){
                secondLayerBiases[entry] = Double.valueOf(secondLayerBiasesStr[entry]);
            }

            int inputSize = firstLayerWeights.length/firstLayerBiases.length ;
            NeuralNetwork loadedNetwork = new NeuralNetwork(inputSize, firstLayerBiases.length, secondLayerBiases.length);

            Stage stage = (Stage) beginTraining.getScene().getWindow();
            ourBestAICoach = new EvolutionarySearch();
            ourBestAICoach.playGameOnly(stage, anchorPane, gamingPane, graphPane, loadedNetwork);

        }catch(SQLException s){}
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
        NumberAxis xAxis = new NumberAxis(0,epNumber,epNumber/10);
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
        selection.setText("Your selection: ");
        selection.setFont(Font.font("Arial", 14));
        loadListLabel.setAlignment(Pos.CENTER);
        loadListPane.setVisible(false);
    }

    private void setNNPoolSizeValue(int value){
        NNPoolSize = value;
    }

    private void setEpNumber(int value){
        epNumber = value;
    }

    private TextField getUserInputNN(){ return userInputNN; }
    private TextField getUserInputEpNum(){ return userInputEpNum; }
}

