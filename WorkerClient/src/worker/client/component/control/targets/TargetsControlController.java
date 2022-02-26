package worker.client.component.control.targets;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import worker.client.component.control.ControlPanelController;
import worker.logic.Worker;
import worker.logic.target.TaskTarget;

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

    public void setWorker(Worker worker) {
        this.worker = worker;
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
