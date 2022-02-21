package admin.component.tasks.config;

import admin.component.tasks.TasksListController;
import admin.component.tasks.config.simulation.CompileConfigController;
import admin.component.tasks.config.simulation.SimulationConfigController;
import admin.util.Constants;
import admin.util.HttpClientUtil;
import admin.util.TaskUtil.Selections.TargetSelect;
import admin.util.TaskUtil.Selections.TypeSelect;

import dto.util.DTOUtil;
import dto.execution.config.ConfigDTO;
import dto.execution.config.ExecutionConfigDTO;
import dto.graph.GraphDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static admin.util.Constants.*;


public class TaskConfigController {
    private TasksListController tasksListController;
    private ExecutionConfigDTO taskConfig;
    private TargetSelect targetsSelect = TargetSelect.non;
    private TypeSelect typeSelect = TypeSelect.non;
    private boolean targetsSelectionSubmitted;
    private ObservableList<String> targets;
    private int simPrice;
    private int compPrice;
    private TypeSelect allowedType;

    public void setTasksListController(TasksListController tasksListController) {
        this.tasksListController = tasksListController;
    }

    @FXML
    private CheckBox allTargetCheckBox;
    @FXML
    private TextField taskNameTextField;
    @FXML
    private ListView<String> targetListView;
    @FXML
    private CheckBox customTargetCheckBox;
    @FXML
    private CheckBox whatIfCheckBox;
    @FXML
    private ChoiceBox<String> wayChoice;
    @FXML
    private Button submitTargetButton;
    @FXML
    private ComboBox<String> targetChoice;
    @FXML
    private Label warningTargetsLabel;
    @FXML
    private Label warningTaskTypeLabel;
    @FXML
    private CheckBox simulationCheckBox;
    @FXML
    private CheckBox compileCheckBox;
    @FXML
    private BorderPane taskParamBorderPane;
    @FXML
    private Button finalSubmitButton;
    @FXML
    private TitledPane titledPaneStep1;
    @FXML
    private TitledPane titledPaneStep2;

    @FXML
    public void initialize() {
        taskConfig = new ExecutionConfigDTO();
        wayChoice.getItems().addAll("Depends On", "Required For");
        targetsSelectionSubmitted = false;
        finalSubmitButton.setVisible(false);
        titledPaneStep1.setExpanded(true);
        allowedType = TypeSelect.non;
    }

    public void fetchData(GraphDTO graph) {

        fillCustomTargetsAndWhatIff(graph);
        taskConfig.setGraphName(graph.getName());
        simPrice = graph.getSimulationPrice();
        compPrice = graph.getCompilationPrice();

        if (simPrice == 0) {
            allowedType = TypeSelect.compile;
        } else if (compPrice == 0) {
            allowedType = TypeSelect.simulation;
        } else {
            allowedType = TypeSelect.all;
        }
    }

    private void fillCustomTargetsAndWhatIff(GraphDTO graph) {

        targets = FXCollections.observableArrayList(graph.getTargetsListByName());
        targetListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        targetListView.setItems(targets);
        targetChoice.setItems(targets);
    }

    // -------------------------------------------------------------------
    // Targets
    @FXML
    void allTargetsChecked(ActionEvent event) {
        if (allTargetCheckBox.isSelected()) {
            customTargetCheckBox.setSelected(!allTargetCheckBox.isSelected());
            disableCustomTargets(true);
            disableWhatIfTarget(true);
            whatIfCheckBox.setSelected(!allTargetCheckBox.isSelected());
            targetsSelect = TargetSelect.all;
        } else {
            targetsSelect = TargetSelect.non;
        }
    }

    @FXML
    void customTargetsChecked(ActionEvent event) {
        if (customTargetCheckBox.isSelected()) {
            allTargetCheckBox.setSelected(!customTargetCheckBox.isSelected());
            whatIfCheckBox.setSelected(!customTargetCheckBox.isSelected());
            disableCustomTargets(false);
            disableWhatIfTarget(true);
            targetsSelect = TargetSelect.custom;
        } else {
            disableCustomTargets(true);
            targetsSelect = TargetSelect.non;
        }
    }

    @FXML
    void whatIfTargetChecked(ActionEvent event) {
        if (whatIfCheckBox.isSelected()) {
            disableWhatIfTarget(false);
            allTargetCheckBox.setSelected(!whatIfCheckBox.isSelected());
            customTargetCheckBox.setSelected(!whatIfCheckBox.isSelected());
            disableCustomTargets(true);
            targetsSelect = TargetSelect.whatif;
        } else {
            disableWhatIfTarget(true);
            targetsSelect = TargetSelect.non;
        }
    }

    @FXML
    void buttonTargetClicked(ActionEvent event) {
        switch (targetsSelect) {
            case non:
                warningTargetsLabel.setText("You have to select an option");
                break;
            case all:
                taskConfig.setAllTargets(true);
                break;
            case custom:
                if (targetListView.getSelectionModel().getSelectedItems().size() == 0) {
                    targetsSelect = TargetSelect.non;
                    customTargetCheckBox.setSelected(false);
                    warningTargetsLabel.setText("You didn't chose any targets.");
                } else {
                    taskConfig.setCustomTargets(true);
                    updateCustomTargetInTaskConfig();
                }
                break;
            case whatif:
                if (targetChoice.getSelectionModel().isEmpty() || wayChoice.getSelectionModel().isEmpty()) {
                    targetsSelect = TargetSelect.non;
                    whatIfCheckBox.setSelected(false);
                    warningTargetsLabel.setText("You didn't chose a target and/or a relation (way).");
                } else {
                    taskConfig.setWhatIfTarget(true);
                    updateWhatIfInTaskConfig();
                }
                break;
        }

        if (targetsSelect != TargetSelect.non) {
            if (!taskNameTextField.getText().isEmpty()) {
                taskConfig.setName(taskNameTextField.getText());
                taskConfig.setCreatingUser(tasksListController.getUsername());
                warningTargetsLabel.setText("Settings submitted!");
                submitTargetButton.setDisable(true);
                disableAllTargets();
                targetsSelectionSubmitted = true;
                titledPaneStep1.setExpanded(false);
                titledPaneStep2.setExpanded(true);
            } else {
                warningTargetsLabel.setText("Please enter task's name");
            }
        }
    }

    private void updateWhatIfInTaskConfig() {
        switch (wayChoice.getSelectionModel().getSelectedItem()) {
            case "Depends On":
                taskConfig.setWhatIfTargetRelation(DTOUtil.RelationTypeDTO.DependsOn);
                break;
            case "Required For":
                taskConfig.setWhatIfTargetRelation(DTOUtil.RelationTypeDTO.RequiredFor);
                break;
        }
        taskConfig.setWhatIfTargetName(targetChoice.getSelectionModel().getSelectedItem());
    }

    private void updateCustomTargetInTaskConfig() {
        ObservableList<String> selectedTargets = targetListView.getSelectionModel().getSelectedItems();
        List<String> list = new ArrayList<>(selectedTargets);
        System.out.println(list);
        taskConfig.setCustomTargetsList(list);
    }

    private void disableAllTargets() {
        allTargetCheckBox.setDisable(true);
        customTargetCheckBox.setDisable(true);
        targetListView.setDisable(true);
        whatIfCheckBox.setDisable(true);
        targetChoice.setDisable(true);
        wayChoice.setDisable(true);
    }

    private void disableCustomTargets(boolean bool) {
        targetListView.setDisable(bool);
    }

    private void disableWhatIfTarget(boolean bool) {
        targetChoice.setDisable(bool);
        wayChoice.setDisable(bool);
    }

    // -------------------------------------------------------------------
    // Type

    @FXML
    void simulationChecked(ActionEvent event) {
        if (simulationCheckBox.isSelected()) {
            compileCheckBox.setSelected(!simulationCheckBox.isSelected());
            if (allowedType != TypeSelect.compile) {
                warningTaskTypeLabel.setText("");
                typeSelect = TypeSelect.simulation;
                taskConfig.setExecutionType(DTOUtil.ExecutionTypeDTO.Simulation);
                taskConfig.setPrice(simPrice);
                URL url = getClass().getResource(SIMULATION_CONFIG_FXML_NAME);
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(url);
                    AnchorPane simComponent = fxmlLoader.load();
                    SimulationConfigController simulationController = fxmlLoader.getController();
                    simulationController.setTaskConfigController(this);
                    taskParamBorderPane.setCenter(simComponent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                warningTaskTypeLabel.setText("Simulation execution isn't allowed for this graph");
                taskParamBorderPane.setCenter(null);
            }
        } else {
            typeSelect = TypeSelect.non;
        }
    }


    @FXML
    void compileChecked(ActionEvent event) {
        if (compileCheckBox.isSelected()) {
            simulationCheckBox.setSelected(!compileCheckBox.isSelected());
            if (allowedType != TypeSelect.simulation) {
                warningTaskTypeLabel.setText("");
                typeSelect = TypeSelect.compile;
                taskConfig.setExecutionType(DTOUtil.ExecutionTypeDTO.Compilation);
                taskConfig.setPrice(compPrice);
                URL url = getClass().getResource(COMPILE_CONFIG_FXML_NAME);
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(url);
                    AnchorPane compileComponent = fxmlLoader.load();
                    CompileConfigController compileController = fxmlLoader.getController();
                    compileController.setTaskConfigController(this);
                    taskParamBorderPane.setCenter(compileComponent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                warningTaskTypeLabel.setText("Compilation execution isn't allowed for this graph");
                taskParamBorderPane.setCenter(null);
            }
        } else {
            typeSelect = TypeSelect.non;
        }
    }

    @FXML
    void buttonFinalSubmitClicked(ActionEvent event) {
        if (typeSelect != TypeSelect.non) {
            if (targetsSelectionSubmitted) {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.close();
                uploadExecutionRequest();
            } else {
                warningTaskTypeLabel.setText("Please complete step 1");
            }
        } else {
            warningTaskTypeLabel.setText("You have to select an option");
        }
    }

    private void uploadExecutionRequest() {
        RequestBody body = RequestBody.create(GSON_INST.toJson(taskConfig), MediaType.parse("application/json"));
        String finalUrl = HttpUrl
                .parse(UPLOAD_TASK)
                .newBuilder()
                .addQueryParameter(GRAPH_TASK, YES)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithBody(finalUrl, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> tasksListController.updateGraphsListMsgLabel("Failing to upload task", true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    try {
                        tasksListController.updateGraphsListMsgLabel(response.body().string(), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public void updateConfig(ConfigDTO config) {
        this.taskConfig.setConfigDTO(config);
    }

    public void showFinalSubmit() {
        finalSubmitButton.setVisible(true);
    }
}
