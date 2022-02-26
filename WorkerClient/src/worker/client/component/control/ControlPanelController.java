package worker.client.component.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import worker.client.component.control.targets.TargetsControlController;
import worker.client.component.control.tasks.TaskControlController;
import worker.client.component.main.WorkerMainController;
import worker.logic.Worker;
import worker.logic.target.TaskTarget;
import worker.logic.task.WorkerExecution;

import java.io.IOException;
import java.net.URL;

import static worker.client.util.Constants.TARGETS_CONTROL_FXML_RESOURCE_LOCATION;
import static worker.client.util.Constants.TASK_CONTROL_FXML_RESOURCE_LOCATION;

public class ControlPanelController {
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private Button tasksButton;
    @FXML
    private Label busyThreadsLabel;
    @FXML
    private Button targetsButton;

    private IntegerProperty busyThreads;
    private final BooleanProperty tasksPressed;
    private TaskControlController taskControlController;
    private Parent taskControlComponent;
    private TargetsControlController targetsControlController;
    private Parent targetsControlComponent;
    private WorkerMainController workerMainController;
    private Worker worker;

    public void setBusyThreadsProperty(IntegerProperty busyThreads) {
        this.busyThreads = busyThreads;
        busyThreadsLabel.textProperty().bind(Bindings.concat(busyThreads.asString(), "/", worker.getThreadsCount()));
    }

    public ControlPanelController() {
        tasksPressed = new SimpleBooleanProperty(true);
    }

    @FXML
    public void initialize() {
        tasksButton.disableProperty().bind(tasksPressed);
        targetsButton.disableProperty().bind(tasksPressed.not());
        loadTasks();
        loadTargets();
        setMainPanelTo(taskControlComponent);
    }

    private void loadTasks() {
        URL dashboardUrl = getClass().getResource(TASK_CONTROL_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(dashboardUrl);
            taskControlComponent = fxmlLoader.load();
            taskControlController = fxmlLoader.getController();
            taskControlController.setControlPanelController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTargets() {
        URL dashboardUrl = getClass().getResource(TARGETS_CONTROL_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(dashboardUrl);
            targetsControlComponent = fxmlLoader.load();
            targetsControlController = fxmlLoader.getController();
            targetsControlController.setControlPanelController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    @FXML
    void backButtonClicked(ActionEvent event) {
        workerMainController.pauseTasksControlRefresher();
        // pause targets ??
        workerMainController.switchBackToDashboard();
    }

    @FXML
    void targetsButtonClicked(ActionEvent event) {
        updateMessage("", false);
        setMainPanelTo(targetsControlComponent);
        workerMainController.pauseTasksControlRefresher();
        // resume targets ??
        tasksPressed.set(false);
    }

    @FXML
    void tasksButtonClicked(ActionEvent event) {
        updateMessage("", false);
        setMainPanelTo(taskControlComponent);
        workerMainController.resumeTasksControlRefresher();
        // pause targets ??
        tasksPressed.set(true);

    }

    public void pauseTasksControlRefresher() {
        workerMainController.pauseTasksControlRefresher();
    }

    public void resumeTasksControlRefresher() {
        workerMainController.resumeTasksControlRefresher();
    }


    public void setWorkerMainController(WorkerMainController workerMainController) {
        this.workerMainController = workerMainController;
    }

    public void updateMessage(String msg, boolean isError) {
        workerMainController.updateMessage(msg, isError);
    }

    public void updateTargetsTable(ObservableList<TaskTarget> list) {
        targetsControlController.updateTargetsTable(list);
    }

    public void updateTasksTable(ObservableList<WorkerExecution> list) {
        taskControlController.updateTaskTable(list);
    }

    public void setWorkerForAll(Worker worker) {
        this.worker = worker;
        taskControlController.setWorker(worker);
        targetsControlController.setWorker(worker);
    }
}
