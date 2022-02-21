package worker.client.component.tasks.list;

import dto.execution.ExecutionDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import worker.client.component.dashboard.WorkerDashboardController;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;

import static worker.client.util.Constants.REFRESH_RATE;

public class TasksListController implements Closeable {
    @FXML
    private TableColumn<ExecutionDTO, String> taskNameCol;
    @FXML
    private TableColumn<ExecutionDTO, String> creatingUserCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskTypeCol;
    @FXML
    private TableColumn<ExecutionDTO, String> totalTargetsCol;
    @FXML
    private TableColumn<ExecutionDTO, String> independentCol;
    @FXML
    private TableColumn<ExecutionDTO, String> leafCol;
    @FXML
    private TableColumn<ExecutionDTO, String> middleCol;
    @FXML
    private TableColumn<ExecutionDTO, String> rootCol;
    @FXML
    private TableColumn<ExecutionDTO, String> pricePerTargetCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskStatusCol;
    @FXML
    private TableColumn<ExecutionDTO, Integer> taskWorkersCountCol;
    @FXML
    private TableColumn<ExecutionDTO, Boolean> isWorkerListedCol;
    @FXML
    private TableView<ExecutionDTO> tasksTable;

    private WorkerDashboardController workerDashboardController;
    private final BooleanProperty autoUpdate;
    private Timer timer;
    private TasksListRefresher tasksListRefresher;
    private String currentSelectedTaskName;

    public TasksListController() {
        autoUpdate = new SimpleBooleanProperty(true);
        currentSelectedTaskName = null;
    }

    @FXML
    public void initialize() {
        tasksTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tasksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                currentSelectedTaskName = null;
            } else {
                currentSelectedTaskName = newValue.getName();
            }
        });
        tasksTableInitialize();
    }

    private void tasksTableInitialize() {
        taskNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        creatingUserCol.setCellValueFactory(new PropertyValueFactory<>("creatingUser"));
        taskTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().name()));
        totalTargetsCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getIndependentCount()
                + cellData.getValue().getGraphDTO().getLeafCount()
                + cellData.getValue().getGraphDTO().getRootCount()
                + cellData.getValue().getGraphDTO().getMiddleCount())));
        independentCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getIndependentCount())));
        leafCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getLeafCount())));
        middleCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getMiddleCount())));
        rootCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getRootCount())));
        pricePerTargetCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        taskStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        taskWorkersCountCol.setCellValueFactory(new PropertyValueFactory<>("workersCount"));
        isWorkerListedCol.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getWorkers().contains(workerDashboardController.getUsername())));
    }

    public void setWorkerDashboardController(WorkerDashboardController workerDashboardController) {
        this.workerDashboardController = workerDashboardController;
    }

    private void updateTasksTable(List<ExecutionDTO> taskDTOS) {
        Platform.runLater(() -> {
            ObservableList<ExecutionDTO> items = tasksTable.getItems();
            items.clear();
            items.addAll(taskDTOS);
        });
    }

    public void updateTasksListMsgLabel(String s, boolean isError) {
        Platform.runLater(() -> workerDashboardController.updateMessage(s, isError));
    }

    public void startTaskListRefresher() {
        tasksListRefresher = new TasksListRefresher(autoUpdate, this::updateTasksTable, s -> updateTasksListMsgLabel(s, false));
        timer = new Timer();
        timer.schedule(tasksListRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    public String getSelectedTaskName() {
        return currentSelectedTaskName;
    }

    public void resumeRefresher() {
        autoUpdate.set(true);
    }

    public void pauseRefresher() {
        autoUpdate.set(false);
    }

    public void close() {
        tasksTable.getItems().clear();
        if (tasksListRefresher != null && timer != null) {
            tasksListRefresher.cancel();
            timer.cancel();
        }
    }
}
