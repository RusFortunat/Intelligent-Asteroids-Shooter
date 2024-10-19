package root.intelligentasteroidsshooter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class RestartViewController {
    private String newRecordName;

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

        // now lest fill TableColumn properly; i followed this guide: https://jenkov.com/tutorials/javafx/tableview.html
        TableColumn<RecordHolders, String> column1 =  new TableColumn<>("Name");
        TableColumn<RecordHolders, String> column2 =  new TableColumn<>("Score");
        column1.setCellValueFactory( new PropertyValueFactory<>("name"));
        column2.setCellValueFactory( new PropertyValueFactory<>("score"));

        recordTable.getColumns().add(column1);
        recordTable.getColumns().add(column2);
        column1.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column2.prefWidthProperty().bind(recordTable.widthProperty().multiply(0.49));
        column1.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px;");
        column2.setStyle( "-fx-alignment: CENTER; -fx-font-size: 14px;");
        column1.setResizable(false);
        column2.setResizable(false);

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
                        setStyle("-fx-text-fill: red !important;"); // doesn't work for whatever reason, even with !important flag
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
        yesButton.setOnMousePressed(e->{
            if(e.getButton()== MouseButton.PRIMARY){
                Stage stage = (Stage) yesButton.getScene().getWindow();
                stage.close();
                Stage singlePlayer = new Stage();
                SinglePlayerView singlePlayerView = new SinglePlayerView();
                try{
                    singlePlayerView.start(singlePlayer);
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }
        });
    }

    @FXML
    protected void onCloseButtonClick() {
        closeButton.setOnMousePressed(e->{
            if(e.getButton()== MouseButton.PRIMARY){
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    public TextField getName(){return recordName;}

    @FXML
    public void clearTable(){
        recordTable.getItems().clear();
        recordTable.getColumns().clear();
    }
}
