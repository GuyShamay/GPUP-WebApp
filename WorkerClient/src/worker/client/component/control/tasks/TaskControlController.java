package worker.client.component.control.tasks;

import dto.execution.ExecutionDTO;
import dto.execution.RegisteredExecutionDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import worker.client.component.control.ControlPanelController;
import worker.client.util.TaskUtil;
import worker.logic.Worker;
import worker.logic.task.WorkerExecution;
import worker.logic.task.WorkerExecutionStatus;

public class TaskControlController {
    @FXML
    private Button resumeButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button unregisterButton;
    @FXML
    private Button actionsButton;
    @FXML
    private TableColumn<WorkerExecution, String> nameCol;
    @FXML
    private TableColumn<WorkerExecution, String> statusCol;
    @FXML
    private TableColumn<WorkerExecution, Integer> workersCol;
    @FXML
    private TableColumn<WorkerExecution, String> progressCol;
    @FXML
    private TableColumn<WorkerExecution, Integer> processedTargetsCol;
    @FXML
    private TableColumn<WorkerExecution, Integer> totalCreditCol;
    @FXML
    private TableView<WorkerExecution> tasksTable;

    private ControlPanelController controlPanelController;
    private String selectedTaskName;
    private Worker worker;
    private final BooleanProperty actionsPressed;
    private final BooleanProperty isSelectedPaused;

    public TaskControlController() {
        actionsPressed = new SimpleBooleanProperty(false);
        isSelectedPaused = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize() {
        actionsButton.disableProperty().bind(actionsPressed);
        cancelButton.disableProperty().bind(actionsPressed.not());
        unregisterButton.disableProperty().bind(actionsPressed.not());
        resumeButton.visibleProperty().bind(isSelectedPaused.and(actionsPressed));
        pauseButton.visibleProperty().bind(isSelectedPaused.not().and(actionsPressed));

        tasksTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tasksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedTaskName = null;
                isSelectedPaused.set(false);
            } else {
                selectedTaskName = newValue.getName();
                if (worker.getWorkerExecutionStatus(selectedTaskName).equals(WorkerExecutionStatus.Paused)) {
                    isSelectedPaused.set(true);
                }
            }
        });
        tasksTableInitialize();
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    private void tasksTableInitialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        totalCreditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        processedTargetsCol.setCellValueFactory(new PropertyValueFactory<>("doneTargets"));
        workersCol.setCellValueFactory(new PropertyValueFactory<>("workersCount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("executionStatus"));
        progressCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%d / 100", (int) (cellData.getValue().getProgress() * 100))));
    }

    @FXML
    void actionsButtonClicked(ActionEvent event) {
        controlPanelController.updateMessage("", false);
        actionsPressed.set(true);
        // label -> please select task
        controlPanelController.pauseTasksControlRefresher();
    }

    @FXML
    void cancelButtonClicked(ActionEvent event) {
        actionsPressed.set(false);
    }

    @FXML
    void pauseButtonClicked(ActionEvent event) {
        worker.pauseWorkerExecution(selectedTaskName);
        controlPanelController.resumeTasksControlRefresher();
        actionsPressed.set(false);
    }

    @FXML
    void resumeButtonClicked(ActionEvent event) {
        worker.resumeWorkerExecution(selectedTaskName);
        controlPanelController.resumeTasksControlRefresher();
        actionsPressed.set(false);
    }

    @FXML
    void unregisterButtonClicked(ActionEvent event) {
        ///// Alert Pop Up!!!!!

        if (TaskUtil.confirmationAlert("Unregister Task", "Are you sure you want to unregister task: " + selectedTaskName + "?")) {
            worker.unregisterWorkerExecution(selectedTaskName);
            controlPanelController.resumeTasksControlRefresher();
            actionsPressed.set(false);
        }
    }

    public void updateTaskTable(ObservableList<WorkerExecution> newItems) { // will be called from main controller->refresher: controlPanel->this
        Platform.runLater(() -> {
            ObservableList<WorkerExecution> items = tasksTable.getItems();
            items.clear();
            items.addAll(newItems);

        });
    }

    public void setControlPanelController(ControlPanelController controller) {
        this.controlPanelController = controller;
    }

    public void setActions(boolean b) {
        actionsPressed.set(b);
    }
}
