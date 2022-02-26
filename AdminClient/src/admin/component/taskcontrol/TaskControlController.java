package admin.component.taskcontrol;

import admin.component.main.AdminMainController;
import admin.util.Constants;
import admin.util.GraphUtil;
import admin.util.HttpClientUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.execution.ExecutionDTO;
import dto.execution.LogsWithVersion;
import dto.execution.RunExecutionDTO;
import dto.target.TargetDTO;
import dto.util.DTOUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.Duration;
import java.util.Objects;
import java.util.Timer;
import java.util.stream.Collectors;

import static admin.util.Constants.*;

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
    @FXML
    private Label taskNameLabel;
    @FXML
    private Label graphNameLabel;
    @FXML
    private Label workersLabel;
    @FXML
    private Label ilmrLabel;

    private Timer TasksTimer;
    private Timer outputAreaTimer;
    private OutputAreaRefresher outputAreaRefresher;
    private IntegerProperty outputAreaVersion;

    private ExecutionDTO currentTask;
    private AdminMainController adminMainController;
    private RunTaskRefresher runTaskRefresher;

    private final BooleanProperty runTaskAutoUpdate;
    private final BooleanProperty isEnded;
    private final BooleanProperty isRunning;
    private  TargetDTO pickedTarget;

    public TaskControlController() {
        runTaskAutoUpdate = new SimpleBooleanProperty(true);
        isRunning = new SimpleBooleanProperty(false);
        isEnded = new SimpleBooleanProperty(false);
        outputAreaVersion = new SimpleIntegerProperty(0);
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
        taskNameLabel.setText(currentTask.getName());
        labelTotal.setText(String.valueOf(currentTask.getGraphDTO().getTargetCount()));
        graphNameLabel.setText(currentTask.getGraphDTO().getName());
        ilmrLabel.setText(String.format("%d, %d, %d, %d",
                currentTask.getGraphDTO().getIndependentCount(),
                currentTask.getGraphDTO().getLeafCount(),
                currentTask.getGraphDTO().getMiddleCount(),
                currentTask.getGraphDTO().getRootCount()));
    }

    private void startRunTaskRefresher() {
        runTaskRefresher = new RunTaskRefresher(currentTask.getName(), runTaskAutoUpdate, this::updateTask, s -> updateTaskControlMsgLabel(s, false));
        TasksTimer = new Timer();
        TasksTimer.schedule(runTaskRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    public void startOutputRefresher() {
        outputAreaRefresher = new OutputAreaRefresher(currentTask.getName(), outputAreaVersion, this::updateOutputArea);
        outputAreaTimer = new Timer();
        outputAreaTimer.schedule(outputAreaRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    private void updateOutputArea(LogsWithVersion logsWithVersion) {
        if (logsWithVersion.getVersion() != outputAreaVersion.get()) {
            String deltaChatLines = logsWithVersion
                    .getEntries()
                    .stream()
                    .map(singleLog -> String.format("%s\n---------------------------\n", singleLog)).collect(Collectors.joining());

            Platform.runLater(() -> {
                outputAreaVersion.set(logsWithVersion.getVersion());
                textAreaOutput.appendText(deltaChatLines);
                textAreaOutput.selectPositionCaret(textAreaOutput.getLength());
                textAreaOutput.deselect();
            });
        }
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
        clearListViews();
        frozenCol.setItems(task.getFrozen());
        waitingCol.setItems(task.getWaiting());
        inProcessCol.setItems(task.getInProcess());
        skippedCol.setItems(task.getSkipped());
        successCol.setItems(task.getSuccess());
        warningsCol.setItems(task.getWarnings());
        failureCol.setItems(task.getFailure());
        // update status:
        workersLabel.setText(String.valueOf(task.getWorkers()));
        String status = task.getStatus();
        updateTaskControlMsgLabel(status, false);
        if (Objects.equals(status, "Stopped") || Objects.equals(status, "Done")) {
            isEnded.set(true);
            isRunning.set(false);
            resumeButton.setVisible(false);
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
        if (!comboBoxTargetPick.getSelectionModel().isEmpty()) {
            String targetName = comboBoxTargetPick.getSelectionModel().getSelectedItem();
            new Thread(()->getTargetRealtime(targetName)).start();
        }
        comboBoxTargetPick.getSelectionModel().clearSelection();
    }

    // --------------------------------------------------------------------------------------------------------------
    // Control Panel:
    @FXML
    void playButtonClicked(ActionEvent event) {

        //-----------------------------//
        play();
        updateTargetPick();
    }

    @FXML
    void pauseButtonClicked(ActionEvent event) {
        isRunning.set(false);
        resumeButton.setVisible(true);
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
        //-----------------------------//
        controlCommand(DTOUtil.ExecutionControlDTO.Stop);
    }

    private void getTargetRealtime(String targetName){
        String finalUrl = HttpUrl.parse(TARGET_REALTIME)
                .newBuilder()
                .addQueryParameter(TASK, currentTask.getName())
                .addQueryParameter(TARGET_NAME, targetName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String target = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(target).getAsJsonObject();
                pickedTarget = GraphUtil.createTargetDTO(jsonObject);

                String infoText = "";
                Duration duration;
                switch (pickedTarget.getRunResult()) {
                    case FROZEN:
                        infoText += "Frozen Until " + pickedTarget.getDependsOnToOpenList().toString() + " Will Finish";
                        break;
                    case WAITING:
                        infoText += "Waiting Already :\n";
                        duration = pickedTarget.getWaitingTimeInMs();
                        infoText += String.format("%d min\n%02d sec\n%02d ms",
                                duration.toMinutes(),
                                duration.getSeconds(),
                                duration.toMillis());
                        break;
                    case SKIPPED:
                        infoText += "Skipped Because " + pickedTarget.getSkippedBecauseList().toString() + " Failed";
                        break;
                    case INPROCESS:
                        infoText += "InProcces Already :\n";
                        duration = pickedTarget.getProcessingTimeInMs();
                        infoText += String.format("%d min\n%02d sec\n%02d ms",
                                duration.toMinutes(),
                                duration.getSeconds(),
                                duration.toMillis());
                        break;
                    case FINISHED:
                        infoText += "Finished With " + pickedTarget.getFinishResult();
                        break;
                }

                String info = infoText;
                Platform.runLater(() -> {
                    labelPickedName.setText(targetName);
                    labelPickedType.setText(pickedTarget.getType().toString());
                    labelPickInfo.setText(info);

                });
            }
        });

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
                        startOutputRefresher();
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
        if (runTaskRefresher != null && TasksTimer != null) {
            runTaskRefresher.cancel();
            TasksTimer.cancel();
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

    private void updateTargetPick() {
        ObservableList<String> list = FXCollections.observableArrayList(currentTask.getGraphDTO().getTargetsListByName());
        comboBoxTargetPick.setItems(list);
    }
}
