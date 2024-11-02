package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

public class RestartViewController {
    @FXML
    private Label scoreText;
    @FXML
    private HBox newRecord;
    public Button yesButton;
    public Button closeButton;
    @FXML
    private TextField recordName;
    @FXML
    private TableView<RecordHolders> recordTable;
    @FXML
    private AnchorPane restartPane;
    @FXML
    private Label recordTableLabel;
    @FXML
    private Label yourName;
    @FXML
    private Label question;

    private String newRecordName;

    public RestartViewController(){
        this.recordTable = new TableView<>();
    }

    @FXML
    protected void setLabels(int score){
        this.scoreText.setText("Final score: " + score);
    }

    @FXML
    protected void showRecordField(boolean value){
        this.newRecord.setVisible(value);
    }

    @FXML
    protected String returnName(){
        return recordName.getText();
    }

    @FXML
    protected void fillTable(List<String> list, String newRecordHolder){

        this.newRecordName = newRecordHolder; // i had to create it to pass String to setRowFactory
        clearTable(); // clear table from previous entries (if the user played the game already)

        // let's get names and scores from our RecordTable database
        List<String> names = list.stream().map(s->{
            String[] parts = s.split(",");
            return parts[1];
        })
       .toList();
        List<String> scores = list.stream().map(s->{
                    String[] parts = s.split(",");
                    return parts[2];
                })
                .toList();

        // now lest fill TableColumn properly; i followed this guide: https://jenkov.com/tutorials/javafx/tableview.html
        TableColumn<RecordHolders, String> column1 =  new TableColumn<>("Name");
        TableColumn<RecordHolders, String> column2 =  new TableColumn<>("Score");
        column1.setCellValueFactory( new PropertyValueFactory<>("name"));
        column2.setCellValueFactory( new PropertyValueFactory<>("score"));
        recordTable.getColumns().add(column1);
        recordTable.getColumns().add(column2);
        for(int i = 0; i < names.size(); i++){
            RecordHolders recordHolder = new RecordHolders(names.get(i), scores.get(i));
            recordTable.getItems().add(recordHolder);
        }
        // if database is new and doesn't have that many entries, fill the empty cells with this
        if(names.size() < 10){
            for(int i=names.size(); i < 10; i++){
                RecordHolders recordHolder = new RecordHolders("EMPTY", "0");
                recordTable.getItems().add(recordHolder);
            }
        }

        // some table styling
        recordTable.setStyle("-fx-background-color: transparent; -fx-text-fill: white; "
                + "-fx-base: rgba(56, 176, 209, 0); ");
        column1.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column2.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column1.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px; -fx-text-fill: white;");
        column2.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px; -fx-text-fill: white;");
        column1.setResizable(false);
        column2.setResizable(false);
        column1.setSortable(false);
        column2.setSortable(false);
        column1.getStyleClass().add("customStyles.css");
        recordTable.setRowFactory(tv -> new TableRow<RecordHolders>() {
            @Override
            protected void updateItem(RecordHolders row, boolean empty) {
                super.updateItem(row, empty);
                if (row != null){
                    if(row.getName().equals(newRecordName)) {
                        setStyle("-fx-background-color: #F08080;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    @FXML
    protected void onYesButtonClick() throws Exception {
        // user wants to play again
        FXMLLoader chooseFighterView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("choose-fighter.fxml"));
        try{
            Scene chooseFighterScene = new Scene(chooseFighterView.load());
            ChooseFighterController startViewController = chooseFighterView.getController();
            startViewController.setBackground();

            Stage chooseFighter = new Stage();
            chooseFighter.setScene(chooseFighterScene);
            chooseFighter.show();
        }catch(Exception em){
            System.out.printf(em.getMessage());
        }

        Stage stage = (Stage) yesButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onCloseButtonClick() {
        // call the start window to let user pick the TrainAI option, if they want
        FXMLLoader startView = new FXMLLoader(IntelligentAsteroidsShooter.class.getResource("start-view.fxml"));
        try{
            Scene startViewScene = new Scene(startView.load());
            StartViewController startViewController = startView.getController();
            startViewController.setBackground();

            Stage startViewStage = new Stage();
            startViewStage.setScene(startViewScene);
            startViewStage.show();
        }catch(Exception em){
            System.out.printf(em.getMessage());
        }

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void setBackground(){
        Image backgroundFile =
                new Image(getClass().getResource("/root/intelligentasteroidsshooter/images/stars_moving.gif").toString());
        BackgroundImage myBI= new BackgroundImage(backgroundFile, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background paneBackgr = new Background(myBI);
        restartPane.setBackground(paneBackgr);
        recordTableLabel.setTextFill(Color.WHITE);
        scoreText.setTextFill(Color.WHITE);
        yourName.setTextFill(Color.WHITE);
        question.setTextFill(Color.WHITE);
    }

    public TextField getName(){return recordName;}

    @FXML
    public void clearTable(){
        recordTable.getItems().clear();
        recordTable.getColumns().clear();
    }
}
