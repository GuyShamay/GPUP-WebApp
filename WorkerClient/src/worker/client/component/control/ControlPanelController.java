package worker.client.component.control;

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
import javafx.scene.layout.BorderPane;
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
    private BorderPane mainBorderPain;
    @FXML
    private Button backButton;
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
        busyThreadsLabel.textProperty().bind(busyThreads.asString());
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
        setMainBorderPainTo(taskControlComponent);
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

    private void setMainBorderPainTo(Parent parent) {
        mainBorderPain.setCenter(parent);
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
        taskControlController.setActions(false);
        setMainBorderPainTo(targetsControlComponent);
        workerMainController.pauseTasksControlRefresher();
        // resume targets ??
        tasksPressed.set(false);
    }

    @FXML
    void tasksButtonClicked(ActionEvent event) {
        updateMessage("", false);
        setMainBorderPainTo(taskControlComponent);
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
