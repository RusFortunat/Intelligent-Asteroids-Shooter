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
        clearTable();
        // list has ID, name, and score and should be already sorted
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
        //System.out.println("scores");
        //System.out.println(scores);

        // now lest fill TableColumn properly; i followed this guide: https://jenkov.com/tutorials/javafx/tableview.html
        TableColumn<RecordHolders, String> column1 =  new TableColumn<>("Name");
        TableColumn<RecordHolders, String> column2 =  new TableColumn<>("Score");
        column1.setCellValueFactory( new PropertyValueFactory<>("name"));
        column2.setCellValueFactory( new PropertyValueFactory<>("score"));

        recordTable.setStyle("-fx-background-color: transparent; -fx-text-fill: white; "
                + "-fx-base: rgba(56, 176, 209, 0); ");
        recordTable.getColumns().add(column1);
        recordTable.getColumns().add(column2);
        column1.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column2.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column1.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px; -fx-text-fill: white;");
        column2.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px; -fx-text-fill: white;");
        column1.setResizable(false);
        column2.setResizable(false);
        column1.setSortable(false);
        column2.setSortable(false);
        column1.getStyleClass().add("customStyles.css");

        for(int i = 0; i < names.size(); i++){
            RecordHolders recordHolder = new RecordHolders(names.get(i), scores.get(i));
            recordTable.getItems().add(recordHolder);
        }
        if(names.size() < 10){
            for(int i=names.size(); i < 10; i++){
                RecordHolders recordHolder = new RecordHolders("EMPTY", "0");
                recordTable.getItems().add(recordHolder);
            }
        }

        recordTable.setRowFactory(tv -> new TableRow<RecordHolders>() {
            @Override
            protected void updateItem(RecordHolders row, boolean empty) {
                super.updateItem(row, empty);
                if (row != null){
                    if(row.getName().equals(newRecordName)) {
                        // none of this worked
                        //this.setTextFill(Color.RED);
                        //setText(row.getName());
                        //setText(row.getScore());
                        //setStyle("red-column");
                        //this.setStyle("-fx-text-fill: red !important;"); // doesn't work for whatever reason, even with !important flag
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
        //BackgroundFill myBF = new BackgroundFill(Color.BLUEVIOLET, new CornerRadii(1),
        //        new Insets(0.0,0.0,0.0,0.0));// or null for the padding
        Background paneBackgr = new Background(myBI);
        restartPane.setBackground(paneBackgr);
        recordTableLabel.setTextFill(Color.WHITE);
        scoreText.setTextFill(Color.WHITE);
        yourName.setTextFill(Color.WHITE);
        question.setTextFill(Color.WHITE);
        //recordName.setStyle("-fx-text-fill: white;");
    }

    public TextField getName(){return recordName;}

    @FXML
    public void clearTable(){
        recordTable.getItems().clear();
        recordTable.getColumns().clear();
    }
}
