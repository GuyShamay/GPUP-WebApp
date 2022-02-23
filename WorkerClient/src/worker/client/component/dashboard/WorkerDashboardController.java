package worker.client.component.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.execution.ExecutionDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import worker.client.component.main.WorkerMainController;
import worker.client.component.tasks.list.TasksListController;
import worker.client.component.users.UsersListController;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;

import javax.swing.text.html.HTML;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static worker.client.util.Constants.TASKS_LIST_FXML_RESOURCE_LOCATION;
import static worker.client.util.Constants.TASK_NAME;

public class WorkerDashboardController implements Closeable {

    @FXML
    private AnchorPane mainPanel;
    @FXML
    private AnchorPane usersListComponent;
    @FXML
    private Button registerButton;
    @FXML
    private Button submitRegisterButton;
    @FXML
    private Button tasksScreenButton;
    @FXML
    private Label submitLabel;

    @FXML
    public UsersListController usersListComponentController;

    private WorkerMainController workerMainController;
    private TasksListController tasksListController;
    private ScrollPane tasksListComponent;
    private final BooleanProperty register;

    public WorkerDashboardController() {
        register = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize() {
        submitRegisterButton.visibleProperty().bind(register);
        submitLabel.visibleProperty().bind(register);
    }

    public void setWorkerMainController(WorkerMainController workerMainController) {
        this.workerMainController = workerMainController;
    }

    public void setActive() {
        usersListComponentController.startListRefresher();
        loadTasksListComponent();
        tasksListController.startTaskListRefresher();

        // load tasks Screen...
    }

    private void loadTasksListComponent() {
        URL taskSListURL = getClass().getResource(TASKS_LIST_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(taskSListURL);
            tasksListComponent = fxmlLoader.load();
            tasksListController = fxmlLoader.getController();
            tasksListController.setWorkerDashboardController(this);
            setMainPanelTo(tasksListComponent);
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
    void registerButtonClicked(ActionEvent event) {
        tasksListController.pauseRefresher();
        register.set(true);
    }

    @FXML
    void submitRegisterButtonClicked(ActionEvent event) {
        if (tasksListController.getSelectedTaskName() != null) {
            registerForTask();
            tasksListController.resumeRefresher();
            register.set(false);
        } else {
            updateMessage("Please select a task", true);
        }
    }

    private void registerForTask() {
        if (isAllowedToRegister(tasksListController.getSelectedTask())) {
            String finalUrl = HttpUrl
                    .parse(Constants.REGISTER)
                    .newBuilder()
                    .addQueryParameter(TASK_NAME, tasksListController.getSelectedTaskName())
                    .build()
                    .toString();
            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() != 200) {
                        Platform.runLater(() -> {
                            try {
                                workerMainController.updateMessage(response.body().string(), true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        // add execution in worker
                        String workerExecutionAsString = response.body().string();
                        //JsonObject jsonObject = JsonParser.parseString(workerExecutionAsString).getAsJsonObject();
                        System.out.println(workerExecutionAsString);
                        workerMainController.registerForTask(JsonParser.parseString(workerExecutionAsString).getAsJsonObject());
                        Platform.runLater(() -> workerMainController.updateMessage("Registered Successfully!", false));
                    }
                }
            });
        } else {
            updateMessage("It is not allowed to register to: " + tasksListController.getSelectedTaskName(), true);
        }
    }

    private boolean isAllowedToRegister(ExecutionDTO selectedTask) {
        String status = selectedTask.getStatus();
        return Objects.equals(status, "New") || Objects.equals(status, "Paused") || Objects.equals(status, "Active");
    }

    @FXML
    void tasksScreenButtonClicked(ActionEvent event) {
        register.set(false);
        // worker main controller -> switch to tasks screen
    }

    public String getUsername() {
        return workerMainController.getUsername();
    }

    public void updateMessage(String msg, boolean isError) {
        workerMainController.updateMessage(msg, isError);
    }

    @Override
    public void close() throws IOException {
        usersListComponentController.close();
        tasksListController.close();
    }
}
