package root.intelligentasteroidsshooter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public RestartViewController(){
        this.recordTable = new TableView<>();
        //this.recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
    protected void fillTable(List<String> list){
        clearTable();
        // list has ID, name, and score and should be already sorted
        System.out.println("We are in the fillTable method");
        List<String> names = list.stream().map(s->{
            String[] parts = s.split(",");
            return parts[1];
        })
       .toList();
        System.out.println("names:");
        System.out.println(names);
        List<String> scores = list.stream().map(s->{
                    String[] parts = s.split(",");
                    return parts[2];
                })
                .toList();
        System.out.println("scores:");
        System.out.println(scores);

        // now lest fill TableColumn properly; i will follow TableColumn Oracle documentation
        TableColumn<RecordHolders, String> column1 =
                new TableColumn<>("Name");

        column1.setCellValueFactory(
                new PropertyValueFactory<>("name"));


        TableColumn<RecordHolders, String> column2 =
                new TableColumn<>("Score");

        column2.setCellValueFactory(
                new PropertyValueFactory<>("score"));


        recordTable.getColumns().add(column1);
        recordTable.getColumns().add(column2);
        column1.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column2.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column1.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px;");
        column2.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px;");

        column1.setResizable(false);
        column2.setResizable(false);

        //List<RecordHolders> recordHolders = new ArrayList<>();
        for(int i = 0; i < names.size(); i++){
            RecordHolders recordHolder = new RecordHolders(names.get(i), scores.get(i));
            recordTable.getItems().add(recordHolder);
        }
    }

    @FXML
    public void clearTable(){
        recordTable.getItems().clear();
        recordTable.getColumns().clear();
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

    public void setName(String value){
        recordName.setText(value);
    }

    public TextField getName(){return recordName;}

}
