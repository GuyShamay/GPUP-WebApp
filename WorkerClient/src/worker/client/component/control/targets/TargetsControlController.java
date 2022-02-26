package worker.client.component.control.targets;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import worker.client.component.control.ControlPanelController;
import worker.logic.Worker;
import worker.logic.target.TaskTarget;
import worker.logic.task.WorkerExecutionStatus;

public class TargetsControlController {
    @FXML
    private TableView<TaskTarget> targetsTable;
    @FXML
    private TableColumn<TaskTarget, String> nameCol;
    @FXML
    private TableColumn<TaskTarget, String> taskCol;
    @FXML
    private TableColumn<TaskTarget, String> taskTypeCol;
    @FXML
    private TableColumn<TaskTarget, String> statusCol;
    @FXML
    private TableColumn<TaskTarget, Integer> payedPriceCol;
    @FXML
    private TableColumn<TaskTarget, String> logsCol;

    private ControlPanelController controlPanelController;
    private Worker worker;

    @FXML
    public void initialize() {

        targetsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        targetsTableInitialize();
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    private void targetsTableInitialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        taskCol.setCellValueFactory(new PropertyValueFactory<>("executionName"));
        logsCol.setCellValueFactory(new PropertyValueFactory<>("logs"));
        payedPriceCol.setCellValueFactory(new PropertyValueFactory<>("payedPrice"));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        taskTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().name()));
    }

    public void setControlPanelController(ControlPanelController controlPanelController) {
        this.controlPanelController = controlPanelController;
    }

    public void updateTargetsTable(ObservableList<TaskTarget> list) {
        Platform.runLater(() -> {
            ObservableList<TaskTarget> items = targetsTable.getItems();
            items.clear();
            items.addAll(list);
        });
    }
}
