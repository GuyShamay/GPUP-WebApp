package admin.component.actions.data;

import admin.component.dashboard.AdminDashboardController;
import dto.graph.GraphDTO;
import dto.target.TargetDTO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GraphDataController {
    @FXML
    private Label totalTargetsLabel;
    @FXML
    private Label independentLabel;
    @FXML
    private Label rootLabel;
    @FXML
    private Label graphNameLabel;
    @FXML
    private Label midLabel;
    @FXML
    private Label leafLabel;
    @FXML
    private TableView<TargetDTO> graphTable;
    @FXML
    private TableColumn<TargetDTO, String> targetCol;
    @FXML
    private TableColumn<TargetDTO, String> typeCol;
    @FXML
    private TableColumn<TargetDTO, Integer> requiredForCol;
    @FXML
    private TableColumn<TargetDTO, Integer> dependsOnCol;
    @FXML
    private TableColumn<TargetDTO, String> dataCol;

    private AdminDashboardController adminDashboardController;

    private GraphDTO currentGraph;

    @FXML
    public void initialize() {
        graphTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        graphTableInitialize();
    }

    private void graphTableInitialize() {
        targetCol.setCellValueFactory(new PropertyValueFactory<TargetDTO, String>("name"));
        dataCol.setCellValueFactory(new PropertyValueFactory<TargetDTO, String>("userData"));
        typeCol.setCellValueFactory(new PropertyValueFactory<TargetDTO, String>("type"));
        dependsOnCol.setCellValueFactory(new PropertyValueFactory<TargetDTO, Integer>("dependsOnCount"));
        requiredForCol.setCellValueFactory(new PropertyValueFactory<TargetDTO, Integer>("requiredForCount"));
    }

    private void updateSumSection() {
        totalTargetsLabel.setText(Integer.toString(currentGraph.getTargetsList().size()));
        leafLabel.setText(Integer.toString(currentGraph.getLeafCount()));
        independentLabel.setText(Integer.toString(currentGraph.getIndependentCount()));
        midLabel.setText(Integer.toString(currentGraph.getMiddleCount()));
        rootLabel.setText(Integer.toString(currentGraph.getRootCount()));
    }

    public void setCurrentGraph(GraphDTO currentGraph) {
        this.currentGraph = currentGraph;
        graphTable.setItems(FXCollections.observableArrayList(currentGraph.getTargetsList()));
        this.graphNameLabel.setText(currentGraph.getName());
        updateSumSection();
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }
}
