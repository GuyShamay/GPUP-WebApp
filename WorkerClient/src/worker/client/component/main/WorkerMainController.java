package worker.client.component.main;

import com.google.gson.JsonObject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import worker.client.component.control.ControlPanelController;
import worker.client.component.control.targets.TargetsControlRefresher;
import worker.client.component.control.tasks.TasksControlRefresher;
import worker.client.component.dashboard.WorkerDashboardController;
import worker.client.component.login.LoginController;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.client.util.TaskUtil;
import worker.logic.Worker;
import worker.logic.task.WorkerExecution;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Timer;

import static worker.client.util.Constants.*;


public class WorkerMainController implements Closeable {
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label totalCreditLabel;

    private Worker worker;
    private StringProperty username;

    private Parent dashboardComponent;
    private Parent controlPanelComponent;
    private WorkerDashboardController workerDashboardController;
    private ControlPanelController controlPanelController;
    private AnchorPane loginComponent;
    private LoginController loginController;
    private TasksControlRefresher tasksControlRefresher;
    private TargetsControlRefresher targetsControlRefresher;

    private Timer targetsTimer;
    private Timer tasksTimer;
    private boolean isLoggedIn;

    // ------------------------------------------------------------------------------------------------------------
    // Controller:
    public WorkerMainController() {
        username = new SimpleStringProperty();
        worker = new Worker();
    }

    @FXML
    public void initialize() {
        isLoggedIn = false;
        totalCreditLabel.textProperty().bind(worker.creditProperty().asString());
        loadLoginPageAndSet();
        loadDashboard();
        loadControlPanel();
    }

    @Override
    public void close() throws IOException {
        workerDashboardController.close();
        if (targetsControlRefresher != null && targetsTimer != null) {
            targetsControlRefresher.cancel();
            targetsTimer.cancel();
        }
        if (tasksControlRefresher != null && tasksTimer != null) {
            tasksControlRefresher.cancel();
            tasksTimer.cancel();
        }

        if (isLoggedIn) {
            unregisterTasks();
            try {
                worker.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logout();
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


    private void unregisterTasks() throws IOException {
        List<String> tasksList = worker.getTasksListByName();
        String tasksAsJson = GSON_INST.toJson(tasksList);
        RequestBody body = RequestBody.create(tasksAsJson, MediaType.parse("application/json"));

        HttpClientUtil.runSyncWithBody(Constants.UNREGISTER, body);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Login Component:

    private void logout() throws IOException {
        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT)
                .newBuilder()
                .addQueryParameter("username", username.get())
                .build()
                .toString();

        HttpClientUtil.runSync(finalUrl);
    }

    private void loadLoginPageAndSet() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            loginController = fxmlLoader.getController();
            loginController.setWorkerMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToDashboardFirstTime() {
        setMainPanelTo(dashboardComponent);
        workerDashboardController.setActive();
        isLoggedIn = true;

    }

    // ------------------------------------------------------------------------------------------------------------
    // Dashboard Component:

    private void loadDashboard() {
        usernameLabel.textProperty().bind(Bindings.concat("Hello ", username, "!"));
        URL loginPageUrl = getClass().getResource(WORKER_DASHBOARD_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            dashboardComponent = fxmlLoader.load();
            workerDashboardController = fxmlLoader.getController();
            workerDashboardController.setWorkerMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------------------------------------------

    public void setUsername(String userName) {
        username.set(userName);
        worker.setName(userName);
    }

    public String getUsername() {
        return worker.getName();
    }

    public void setWorkerThreads(Integer value) {
        worker.initThreadsExecutor(value);
    }

    public boolean isRegisterAny() {
        return worker.isRegisterAny();
    }

    public void updateMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        if (isError) {
            messageLabel.setStyle("-fx-text-fill:#a1121e");
        } else {
            messageLabel.setStyle("-fx-text-fill: green");
        }
    }

    public void registerForTask(JsonObject jsonObject) {
        WorkerExecution execution = TaskUtil.parseToWorkerExecution(jsonObject);
        this.worker.addExecution(execution);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Control Panel

    private void loadControlPanel() {
        URL url = getClass().getResource(CONTROL_PANEL_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(url);
            controlPanelComponent = fxmlLoader.load();
            controlPanelController = fxmlLoader.getController();
            controlPanelController.setWorkerForAll(this.worker);
            controlPanelController.setWorkerMainController(this);
            controlPanelController.setBusyThreadsProperty(worker.busyThreadsProperty());
            startTasksControlRefresher();
            startTargetsControlRefresher();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTargetsControlRefresher() {
        targetsControlRefresher = new TargetsControlRefresher(this::updateTargetsTable);
        targetsTimer = new Timer();
        targetsTimer.schedule(targetsControlRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    private void startTasksControlRefresher() {
        tasksControlRefresher = new TasksControlRefresher(this::updateTasksTable);
        tasksTimer = new Timer();
        tasksTimer.schedule(tasksControlRefresher, CONTROL_REFRESH_RATE, CONTROL_REFRESH_RATE);
    }

    public void pauseTasksControlRefresher() {
        tasksControlRefresher.pause();
        worker.pauseLightRefresher();
        // pause lightWorkerExecutionRefresher
        // boolean property -> false
    }

    public void resumeTasksControlRefresher() {
        tasksControlRefresher.resume();
        worker.resumeLightRefresher();
        // resume lightWorkerExecutionRefresher
        // boolean property -> true
    }


    public void switchToControlPanel() {
        updateMessage("", false);
        setMainPanelTo(controlPanelComponent);
        controlPanelController.resumeTasksControlRefresher();
    }

    public void switchBackToDashboard() {
        updateMessage("", false);
        setMainPanelTo(dashboardComponent);
        workerDashboardController.resumeRefreshers();
    }

    public void updateTargetsTable(Void v) {
        controlPanelController.updateTargetsTable(worker.getObservableTargets());
    }

    public void updateTasksTable(Void v) {
        controlPanelController.updateTasksTable(worker.getObservableWorkerExecutions());
    }
}
