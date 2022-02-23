package admin.component.taskcontrol;

import admin.component.main.AdminMainController;
import admin.util.Constants;
import admin.util.HttpClientUtil;
import dto.execution.ExecutionDTO;
import dto.execution.RunExecutionDTO;
import dto.util.DTOUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;

import static admin.util.Constants.REFRESH_RATE;
import static admin.util.Constants.TASK;

public class TaskControlController implements Closeable {

    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button stopButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ListView<String> frozenCol;
    @FXML
    private ListView<String> waitingCol;
    @FXML
    private ListView<String> skippedCol;
    @FXML
    private ListView<String> failureCol;
    @FXML
    private ListView<String> warningsCol;
    @FXML
    private ListView<String> successCol;
    @FXML
    private ListView<String> inProcessCol;
    @FXML
    private Label taskMessageLabel;
    @FXML
    private Label labelSkipped;
    @FXML
    private Label labelSuccess;
    @FXML
    private Label labelWarnings;
    @FXML
    private Label labelFailure;
    @FXML
    private Label labelTaskStatus;
    @FXML
    private Label labelTotal;
    @FXML
    private ComboBox<String> comboBoxTargetPick;
    @FXML
    private Label labelPickedName;
    @FXML
    private Label labelPickedType;
    @FXML
    private Label labelPickInfo;
    @FXML
    private Label endedTaskLabel;
    @FXML
    private TextArea textAreaOutput;
    @FXML
    private Label labelPickedType1;

    private Timer timer;
    private ExecutionDTO currentTask;
    private AdminMainController adminMainController;
    private RunTaskRefresher runTaskRefresher;

    private final BooleanProperty runTaskAutoUpdate;
    private final BooleanProperty isEnded;
    private final BooleanProperty isRunning;

    public TaskControlController() {
        runTaskAutoUpdate = new SimpleBooleanProperty(true);
        isRunning = new SimpleBooleanProperty(false);
        isEnded = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize() {
        playButton.visibleProperty().bind(isEnded.not());
        endedTaskLabel.visibleProperty().bind(isEnded);
        stopButton.visibleProperty().bind(isRunning);
        pauseButton.visibleProperty().bind(isRunning);
        resumeButton.setVisible(false);
    }

    public void setAdminMainController(AdminMainController adminMainController) {
        this.adminMainController = adminMainController;
    }

    public void setCurrentTask(ExecutionDTO currentTask) {
        this.currentTask = currentTask;

    }

    private void startRunTaskRefresher() {
        runTaskRefresher = new RunTaskRefresher(currentTask.getName(), runTaskAutoUpdate, this::updateTask, s -> updateTaskControlMsgLabel(s, false));
        timer = new Timer();
        timer.schedule(runTaskRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    private void updateTaskControlMsgLabel(String s, boolean isError) {
        Platform.runLater(() -> {
            taskMessageLabel.setText(s);
            if (isError) {
                taskMessageLabel.setStyle("-fx-text-fill:#a1121e");
            } else {
                taskMessageLabel.setStyle("-fx-text-fill: green");
            }
        });
    }

    private void updateTask(RunExecutionDTO task) {
        System.out.println("update task");
        // update lists views
        frozenCol.setItems(task.getFrozen());
        waitingCol.setItems(task.getWaiting());
        inProcessCol.setItems(task.getInProcess());
        skippedCol.setItems(task.getSkipped());
        successCol.setItems(task.getSuccess());
        warningsCol.setItems(task.getWarnings());
        failureCol.setItems(task.getFailure());
        // update status:
        String status = task.getStatus();
        System.out.println(status);
        //DEBUG:
        updateTaskControlMsgLabel(status, false);
        if (Objects.equals(status, "Stopped") || Objects.equals(status, "Done")) {
            isEnded.set(true);
            runTaskAutoUpdate.set(false); // stop the refresher
            //
            // Ask from server the task results
            //
        }
        // update progress:
        Platform.runLater(() -> progressBar.setProgress(task.getProgress()));
    }

    // --------------------------------------------------------------------------------------------------------------
    @FXML
    void backToDashboardButtonClicked(ActionEvent event) {
        adminMainController.switchToDashboard();
    }

    @FXML
    void buttonGetStatusClicked(ActionEvent event) {
        // ask data about target from server
    }

    // --------------------------------------------------------------------------------------------------------------
    // Control Panel:
    @FXML
    void playButtonClicked(ActionEvent event) {

        //-----------------------------//
        play();
    }

    @FXML
    void pauseButtonClicked(ActionEvent event) {
        isRunning.set(false);
        resumeButton.setVisible(true);
        //runTaskAutoUpdate.set(false);
        //-----------------------------//
        controlCommand(DTOUtil.ExecutionControlDTO.Pause);
    }

    @FXML
    void resumeButtonClicked(ActionEvent event) {
        isRunning.set(true);
        resumeButton.setVisible(false);
        //-----------------------------//
        controlCommand(DTOUtil.ExecutionControlDTO.Resume);

    }

    @FXML
    void stopButtonClicked(ActionEvent event) {
        isRunning.set(false);
        isEnded.set(true);
        resumeButton.setVisible(false);
        //runTaskAutoUpdate.set(false);
        //-----------------------------//
        controlCommand(DTOUtil.ExecutionControlDTO.Stop);

    }

    private void play() {
        String finalUrl = HttpUrl
                .parse(Constants.PLAY_EXECUTION)
                .newBuilder()
                .addQueryParameter(TASK, currentTask.getName())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                updateTaskControlMsgLabel("Error: failed request", true);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String msg = response.body().string();
                    updateTaskControlMsgLabel(msg, true);
                } else { // Play started successfully

                    Platform.runLater(() -> {
                        isRunning.set(true);
                        playButton.setDisable(true);
                        startRunTaskRefresher();
                    });
                }
            }
        });
    }

    private void controlCommand(DTOUtil.ExecutionControlDTO command) {
        String finalUrl = HttpUrl
                .parse(Constants.EXECUTION_CONTROL)
                .newBuilder()
                .addQueryParameter(TASK, currentTask.getName())
                .build()
                .toString();

        RequestBody body = RequestBody.create(String.valueOf(command), MediaType.parse("application/json"));

        HttpClientUtil.runAsyncWithBody(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                updateTaskControlMsgLabel("Error: failed request", true);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String msg = response.body().string();
                    updateTaskControlMsgLabel(msg, true);

                }
            }
        });
    }

    @Override
    public void close() {
        clearListViews();
        if (runTaskRefresher != null && timer != null) {
            runTaskRefresher.cancel();
            timer.cancel();
        }
    }

    private void clearListViews() {
        failureCol.getItems().clear();
        frozenCol.getItems().clear();
        inProcessCol.getItems().clear();
        skippedCol.getItems().clear();
        warningsCol.getItems().clear();
        waitingCol.getItems().clear();
        successCol.getItems().clear();
    }
}
