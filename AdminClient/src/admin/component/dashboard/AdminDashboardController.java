package admin.component.dashboard;

import admin.component.actions.circuit.CircuitController;
import admin.component.actions.data.GraphDataController;
import admin.component.actions.path.PathsController;
import admin.component.actions.whatif.WhatIfController;
import admin.component.graphs.GraphListController;
import admin.component.main.AdminMainController;
import admin.component.tasks.TasksListController;
import admin.component.users.UsersListController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Supplier;

import static admin.util.Constants.*;

public class AdminDashboardController implements Closeable {

    @FXML
    private Button backButton;
    @FXML
    private Button taskControlButton;
    @FXML
    private Button actionsButton;
    @FXML
    private Button graphsButton;
    @FXML
    private Button tasksButton;
    @FXML
    private Button createTaskFromTaskButton;
    @FXML
    private Button createTaskFromGraphButton;
    @FXML
    private Button graphDataButton;
    @FXML
    private Button loadGraphButton;
    @FXML
    private Button findPathButton;
    @FXML
    private Button findCircuitButton;
    @FXML
    private Button whatIfButton;
    @FXML
    private AnchorPane dashboardPanel;
    @FXML
    private AnchorPane usersListComponent;
    @FXML
    public UsersListController usersListComponentController;
    @FXML
    private GridPane graphsComponent;
    @FXML
    private GraphListController graphsComponentController;

    private AdminMainController adminMainController;
    private TasksListController tasksComponentController;
    private GridPane tasksComponent;
    private final BooleanProperty actionsPressed;
    private final BooleanProperty graphsPressed;
    private final BooleanProperty tasksPressed;

    public AdminDashboardController() {
        actionsPressed = new SimpleBooleanProperty(false);
        graphsPressed = new SimpleBooleanProperty(true);
        tasksPressed = new SimpleBooleanProperty(false);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Controller:
    @FXML
    public void initialize() {
        backButton.setVisible(false);
        graphsButton.setDisable(true);
        if (graphsComponentController != null) {
            graphsComponentController.setAdminDashboardController(this);
            bindActions();
        }
    }

    @Override
    public void close() {
        usersListComponentController.close();
        graphsComponentController.close();
        if (tasksComponentController != null) {
            tasksComponentController.close();
        }
    }

    private void bindActions() {
        actionsButton.disableProperty().bind(graphsComponentController.isEmptyTableProperty());

        graphDataButton.disableProperty().bind(actionsPressed.not());
        findPathButton.disableProperty().bind(actionsPressed.not());
        findCircuitButton.disableProperty().bind(actionsPressed.not());
        whatIfButton.disableProperty().bind(actionsPressed.not());
        createTaskFromTaskButton.disableProperty().bind(actionsPressed.not());
        createTaskFromGraphButton.disableProperty().bind(actionsPressed.not());
        taskControlButton.disableProperty().bind(actionsPressed.not());

        graphDataButton.visibleProperty().bind(graphsPressed);
        findPathButton.visibleProperty().bind(graphsPressed);
        findCircuitButton.visibleProperty().bind(graphsPressed);
        whatIfButton.visibleProperty().bind(graphsPressed);
        createTaskFromGraphButton.visibleProperty().bind(graphsPressed);

        createTaskFromTaskButton.visibleProperty().bind(tasksPressed);
        taskControlButton.visibleProperty().bind(tasksPressed);
    }

    public void setAdminMainController(AdminMainController adminMainController) {
        this.adminMainController = adminMainController;
    }

    public void setActive() {
        usersListComponentController.startListRefresher();
        graphsComponentController.startGraphsListRefresher();
        loadTasksListComponent();
        tasksComponentController.startTaskListRefresher();
        createTaskFromTaskButton.disableProperty().bind(tasksComponentController.createNewTaskProperty().not());

    }

    public void setPanelTo(Parent pane) {
        dashboardPanel.getChildren().clear();
        dashboardPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }


    @FXML
    void graphsButtonClicked(ActionEvent event) {
        graphsPressed.set(true);
        tasksPressed.set(false);
        actionsPressed.set(false);
        graphsButton.setDisable(true);
        tasksButton.setDisable(false);
        tasksComponentController.pauseRefresher();
        graphsComponentController.resumeRefresher();
        setPanelTo(graphsComponent);
    }

    @FXML
    void tasksButtonClicked(ActionEvent event) {
        tasksPressed.set(true);
        graphsPressed.set(false);
        actionsPressed.set(false);
        tasksButton.setDisable(true);
        backButton.setVisible(false);
        graphsButton.setDisable(false);
        tasksComponentController.setCreateNewTask(false);
        graphsComponentController.pauseRefresher();
        tasksComponentController.resumeRefresher();
        setPanelTo(tasksComponent);
    }

    private void loadTasksListComponent() {
        URL taskSListURL = getClass().getResource(TASKS_LIST_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(taskSListURL);
            tasksComponent = fxmlLoader.load();
            tasksComponentController = fxmlLoader.getController();
            tasksComponentController.setAdminDashboardController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ------------------------------------------------------------------------------------------------------------
    // Graphs

    // Actions
    public String getSelectedGraphName() {
        return graphsComponentController.getSelectedGraphName();
    }

    private void actionButtonConfig() {
        backButton.setVisible(true);
        actionsPressed.set(false);
    }

    @FXML
    void backButtonClicked(ActionEvent event) {
        backButton.setVisible(false);
        loadGraphButton.setDisable(false);

        graphsComponentController.resumeRefresher();
        graphsComponentController.updateGraphMsgLabel("", false);
        actionsPressed.set(false);
        setPanelTo(graphsComponent);
    }

    private void doGraphAction(Supplier<Void> supplier) {
        if (graphsComponentController.getSelectedGraphName() != null) {
            loadGraphButton.setDisable(true);
            supplier.get();
        } else {
            graphsComponentController.updateGraphMsgLabel("Please Select a graph", true);
        }
    }

    @FXML
    void graphDataButtonClicked(ActionEvent event) {
        doGraphAction(this::showGraphData);
    }

    private Void showGraphData() {
        actionButtonConfig();
        URL loginPageUrl = getClass().getResource(GRAPH_DATA_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            GridPane graphDataComponent = fxmlLoader.load();
            GraphDataController graphDataController = fxmlLoader.getController();
            graphDataController.setAdminDashboardController(this);
            graphDataController.setCurrentGraph(graphsComponentController.getSelectedItem());
            setPanelTo(graphDataComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void findPathButtonClicked(ActionEvent event) {
        doGraphAction(this::findPaths);
    }

    private Void findPaths() {
        actionButtonConfig();
        URL loginPageUrl = getClass().getResource(FIND_PATH_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            GridPane findPathComponent = fxmlLoader.load();
            PathsController pathsController = fxmlLoader.getController();
            pathsController.setAdminDashboardController(this);
            pathsController.fillTargetsComboBox();
            setPanelTo(findPathComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void findCircuitButtonClicked(ActionEvent event) {
        doGraphAction(this::findCircuit);
    }

    private Void findCircuit() {
        actionButtonConfig();
        URL loginPageUrl = getClass().getResource(FIND_CIRCUIT_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            GridPane findCircuitComponent = fxmlLoader.load();
            CircuitController circuitController = fxmlLoader.getController();
            circuitController.setAdminDashboardController(this);
            circuitController.fillTargetsComboBox();
            setPanelTo(findCircuitComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void whatIfButtonClicked(ActionEvent event) {
        doGraphAction(this::whatIf);
    }

    private Void whatIf() {
        actionButtonConfig();
        URL loginPageUrl = getClass().getResource(WHAT_IF_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            GridPane whatIfComponent = fxmlLoader.load();
            WhatIfController whatIfController = fxmlLoader.getController();
            whatIfController.setAdminDashboardController(this);
            whatIfController.fillTargetsComboBox();
            setPanelTo(whatIfComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    void actionsButtonClicked(ActionEvent event) {
        graphsComponentController.pauseRefresher();
        tasksComponentController.pauseRefresher();
        tasksComponentController.setCreateNewTask(true);
        actionsPressed.set(true);
    }

    // Load Graph
    @FXML
    void loadGraphButtonClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open XML File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Xml Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(btn.getScene().getWindow());
        graphsComponentController.loadFile(selectedFile);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Tasks

    @FXML
    void createTaskFromTaskButtonClicked(ActionEvent event) {
        if (tasksComponentController.getSelectedTaskName() != null) {
            if (tasksComponentController.taskRunType()) {
                tasksComponentController.createTask(tasksComponentController.getSelectedTaskName());
                actionsPressed.set(false);
            } else {
                tasksComponentController.updateTasksListMsgLabel("Please Select From Scratch / Incremental", true);
            }
        } else {
            tasksComponentController.updateTasksListMsgLabel("Please Select a task", true);
        }
    }

    @FXML
    void createTaskFromGraphButtonClicked(ActionEvent event) {
        if (graphsComponentController.getSelectedGraphName() != null) {
            tasksComponentController.createGraphTask(graphsComponentController.getSelectedItem());
            graphsComponentController.resumeRefresher();
            backButton.setVisible(false);
            actionsPressed.set(false);
        } else {
            graphsComponentController.updateGraphMsgLabel("Please Select a task", true);
        }
    }

    @FXML
    void taskControlButtonClicked(ActionEvent event) {
    }

    public String getUsername() {
        return adminMainController.getUsername();
    }

    public void updateGraphsListMsgLabel(String msg, boolean isError) {
        graphsComponentController.updateGraphMsgLabel(msg, isError);
    }
}


