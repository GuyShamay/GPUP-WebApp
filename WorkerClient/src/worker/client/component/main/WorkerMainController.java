package worker.client.component.main;

import com.google.gson.JsonObject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.util.ArrayList;
import java.util.List;

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
    private IntegerProperty threads;

    private Parent dashboardComponent;
    private WorkerDashboardController workerDashboardController;
    private AnchorPane loginComponent;
    private LoginController loginController;
    private boolean isLoggedIn;

    // ------------------------------------------------------------------------------------------------------------
    // Controller:
    public WorkerMainController() {
        username = new SimpleStringProperty();
        threads = new SimpleIntegerProperty();
        worker = new Worker();
    }

    @FXML
    public void initialize() {
        isLoggedIn = false;
        totalCreditLabel.textProperty().bind(worker.creditProperty().asString());
        loadLoginPageAndSet();
        loadDashboard();
    }

    @Override
    public void close() throws IOException {
        workerDashboardController.close();
        if (isLoggedIn) {
            unregisterTasks();
            worker.shutdown();
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

    public IntegerProperty threadsProperty() {
        return threads;
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

    public void switchToDashboard() {
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
    public void setThreads(int threads) {
        this.threads.set(threads);
    }

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

    public int getWorkerFreeThreads() {
        return worker.getFreeThreads();
    }

    public int getWorkerThreads() {
        return worker.getThreadsCount();
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
}
