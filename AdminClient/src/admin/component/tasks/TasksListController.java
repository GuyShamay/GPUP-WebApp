package admin.component.tasks;

import admin.AdminClient;
import admin.component.dashboard.AdminDashboardController;
import admin.component.tasks.config.TaskConfigController;
import admin.util.Constants;
import admin.util.HttpClientUtil;
import dto.execution.ExecutionDTO;
import dto.execution.config.ExecutionConfigDTO;
import dto.execution.config.ExistExecutionConfigDTO;
import dto.graph.GraphDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Timer;

import static admin.util.Constants.*;

public class TasksListController implements Closeable {
    @FXML
    private Label tasksListMsgLabel;
    @FXML
    private TableColumn<ExecutionDTO, String> taskNameCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskCreateByCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskGraphNameCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskIndependentCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskLeafCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskMiddleCol;
    @FXML
    private TableColumn<ExecutionDTO, String> taskRootCol;
    @FXML
    private TableColumn<ExecutionDTO, Integer> taskPriceCol;
    @FXML
    private TableColumn<ExecutionDTO, Integer> taskWorkersCol;
    @FXML
    private TableView<ExecutionDTO> taskTable;
    @FXML
    private CheckBox fromScratchCheckBox;
    @FXML
    private CheckBox incCheckBox;

    private Accordion taskConfigComponent;
    private final BooleanProperty autoUpdate;
    private final BooleanProperty createNewTask;
    private AdminDashboardController adminDashboardController;
    private Timer timer;
    private TasksListRefresher tasksListRefresher;
    private String currentSelectedTaskName;

    public TasksListController() {
        autoUpdate = new SimpleBooleanProperty(true);
        createNewTask = new SimpleBooleanProperty(false);
        currentSelectedTaskName = null;
    }

    @FXML
    public void initialize() {
        taskTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        taskTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                currentSelectedTaskName = null;
            } else {
                currentSelectedTaskName = newValue.getName();
            }
        });
        incCheckBox.visibleProperty().bind(createNewTask);
        fromScratchCheckBox.visibleProperty().bind(createNewTask);
        taskTableInitialize();
    }

    public boolean isCreateNewTask() {
        return createNewTask.get();
    }

    public BooleanProperty createNewTaskProperty() {
        return createNewTask;
    }

    public void setCreateNewTask(boolean createNewTask) {
        this.createNewTask.set(createNewTask);
    }

    @FXML
    void fromScratchChecked(ActionEvent event) {
        if (fromScratchCheckBox.isSelected()) {
            incCheckBox.setSelected(false);
        }
    }

    @FXML
    void incrementalChecked(ActionEvent event) {
        if (incCheckBox.isSelected()) {
            fromScratchCheckBox.setSelected(false);
        }
    }

    private void taskTableInitialize() {
        taskNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        taskCreateByCol.setCellValueFactory(new PropertyValueFactory<>("creatingUser"));
        taskWorkersCol.setCellValueFactory(new PropertyValueFactory<>("workersCount"));
        taskPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        taskGraphNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGraphDTO().getName()));
        taskIndependentCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getIndependentCount())));
        taskLeafCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getLeafCount())));
        taskMiddleCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getMiddleCount())));
        taskRootCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getGraphDTO().getRootCount())));
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    private void updateTasksTable(List<ExecutionDTO> taskDTOS) {
        Platform.runLater(() -> {
            ObservableList<ExecutionDTO> items = taskTable.getItems();
            items.clear();
            items.addAll(taskDTOS);
        });
    }

    public void updateTasksListMsgLabel(String s, boolean isError) {
        Platform.runLater(() -> {
            tasksListMsgLabel.setText(s);
            if (isError) {
                tasksListMsgLabel.setStyle("-fx-text-fill:#a1121e");
            } else {
                tasksListMsgLabel.setStyle("-fx-text-fill: green");
            }
        });
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

    public void createGraphTask(GraphDTO graph) {
        Stage stage = new Stage();

        URL url = getClass().getResource(TASK_FROM_GRAPH_CONFIG_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(url);
            taskConfigComponent = fxmlLoader.load();
            TaskConfigController taskConfigController = fxmlLoader.getController();
            taskConfigController.setTasksListController(this);
            taskConfigController.fetchData(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(taskConfigComponent, 404, 504);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setX(AdminClient.mainStage.getX() + AdminClient.mainStage.getWidth() / 3);
        stage.setY(AdminClient.mainStage.getY() + AdminClient.mainStage.getHeight() / 5);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void createTask() {
        //updateTasksListMsgLabel("success!", false);
        ExistExecutionConfigDTO exeConfigDTO = buildExecutionConfig();
        uploadExecutionRequest(exeConfigDTO);
        postTaskCreating();
    }

    private void uploadExecutionRequest(ExistExecutionConfigDTO exeConfigDTO) {
        RequestBody body = RequestBody.create(GSON_INST.toJson(exeConfigDTO), MediaType.parse("application/json"));
        String finalUrl = HttpUrl
                .parse(UPLOAD_TASK)
                .newBuilder()
                .addQueryParameter(GRAPH_TASK, NO)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithBody(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> updateTasksListMsgLabel("Failing to upload task", true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    try {
                        updateTasksListMsgLabel(response.body().string(), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private ExistExecutionConfigDTO buildExecutionConfig() {
        ExistExecutionConfigDTO exeConfigDTO = new ExistExecutionConfigDTO();
        exeConfigDTO.setBaseName(this.currentSelectedTaskName);
        exeConfigDTO.setIncremental(incCheckBox.isSelected());
        return exeConfigDTO;

    }

    private void postTaskCreating() {
        setCreateNewTask(false); // hide check boxes
        autoUpdate.set(true); // table is updated
        incCheckBox.setSelected(false); // delete selection
        fromScratchCheckBox.setSelected(false);// delete selection
    }

    public String getUsername() {
        return adminDashboardController.getUsername();
    }

    public void updateGraphsListMsgLabel(String msg, boolean isError) {
        adminDashboardController.updateGraphsListMsgLabel(msg, isError);
    }

    public boolean taskRunType() {
        return incCheckBox.isSelected() || fromScratchCheckBox.isSelected();
    }

    @Override
    public void close() {
        taskTable.getItems().clear();
        if (tasksListRefresher != null && timer != null) {
            tasksListRefresher.cancel();
            timer.cancel();
        }
    }

    public String getSelectedTaskCreatingUserName() {
        return taskTable.getSelectionModel().getSelectedItem().getCreatingUser();
    }

    public ExecutionDTO getSelectedTask(){
        return taskTable.getSelectionModel().getSelectedItem();
    }
}
