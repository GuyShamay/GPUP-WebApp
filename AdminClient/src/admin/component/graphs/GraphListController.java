package admin.component.graphs;

import admin.component.dashboard.AdminDashboardController;
import admin.util.HttpClientUtil;
import dto.graph.GraphDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import static admin.util.Constants.REFRESH_RATE;
import static admin.util.Constants.UPLOAD_FILE;

public class GraphListController implements Closeable {
    @FXML
    private Label graphsMsgLabel;
    @FXML
    private TableColumn<GraphDTO, String> graphNameCol;
    @FXML
    private TableColumn<GraphDTO, String> graphUploadByCol;
    @FXML
    private TableColumn<GraphDTO, Integer> graphIndependentCol;
    @FXML
    private TableColumn<GraphDTO, Integer> graphLeafCol;
    @FXML
    private TableColumn<GraphDTO, Integer> graphMiddleCol;
    @FXML
    private TableColumn<GraphDTO, Integer> graphRootCol;
    @FXML
    private TableColumn<GraphDTO, Integer> simulationPriceCol;
    @FXML
    private TableColumn<GraphDTO, Integer> compilePriceCol;
    @FXML
    private TableView<GraphDTO> graphTable;

    private final BooleanProperty autoUpdate;
    private final SimpleBooleanProperty isEmptyTable;
    private AdminDashboardController adminDashboardController;
    private Timer timer;
    private GraphListRefresher graphsRefresher;
    private String currentSelectedGraphName;
    private GraphDTO currentGraph;

    public GraphListController() {
        isEmptyTable = new SimpleBooleanProperty(true);
        autoUpdate = new SimpleBooleanProperty(true);
        currentSelectedGraphName = null;
        currentGraph = null;
    }

    @FXML
    public void initialize() {
        graphTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        graphTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                currentSelectedGraphName = null;
                currentGraph = null;
            } else {
                currentSelectedGraphName = newValue.getName();
                currentGraph = newValue;
            }
        });
        isEmptyTable.bind(Bindings.isEmpty(graphTable.getItems()));
        graphTableInitialize();
    }

    public SimpleBooleanProperty isEmptyTableProperty() {
        return isEmptyTable;
    }

    private void graphTableInitialize() {
        graphNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        graphUploadByCol.setCellValueFactory(new PropertyValueFactory<>("creatingUser"));
        graphIndependentCol.setCellValueFactory(new PropertyValueFactory<>("independentCount"));
        graphLeafCol.setCellValueFactory(new PropertyValueFactory<>("leafCount"));
        graphMiddleCol.setCellValueFactory(new PropertyValueFactory<GraphDTO, Integer>("middleCount"));
        graphRootCol.setCellValueFactory(new PropertyValueFactory<GraphDTO, Integer>("rootCount"));
        simulationPriceCol.setCellValueFactory(new PropertyValueFactory<GraphDTO, Integer>("simulationPrice"));
        compilePriceCol.setCellValueFactory(new PropertyValueFactory<GraphDTO, Integer>("compilationPrice"));
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    private void updateGraphTable(List<GraphDTO> graphDTOS) {
        Platform.runLater(() -> {
            ObservableList<GraphDTO> items = graphTable.getItems();
            items.clear();
            items.addAll(graphDTOS);
        });
    }

    public void updateGraphMsgLabel(String s, boolean isError) {
        Platform.runLater(() -> {
            graphsMsgLabel.setText(s);
            if (isError) {
                graphsMsgLabel.setStyle("-fx-text-fill:#a1121e");
            } else {
                graphsMsgLabel.setStyle("-fx-text-fill: green");
            }
        });
    }

    public void startGraphsListRefresher() {
        graphsRefresher = new GraphListRefresher(autoUpdate, this::updateGraphTable, s -> updateGraphMsgLabel(s, false));
        timer = new Timer();
        timer.schedule(graphsRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    public void loadFile(File selectedFile) {// File selectedFile = new File("C:\\Users\\guysh\\Downloads\\ex2-big.xml"); // For Test
        if (selectedFile != null) {
           /* String finalUrl = HttpUrl
                    .parse(UPLOAD_FILE)
                    .newBuilder()
                    .addQueryParameter("username", adminMainController.getUsername())
                    .build()
                    .toString();*/

            HttpClientUtil.uploadFileAsync(UPLOAD_FILE, selectedFile, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> graphsMsgLabel.setText("Failing connecting to server"));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Platform.runLater(() -> {
                        try {
                            graphsMsgLabel.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

        }
    }

    public String getSelectedGraphName() {
        return currentSelectedGraphName;
    }

    public GraphDTO getSelectedItem() {
        return currentGraph;
    }

    public void resumeRefresher() {
        autoUpdate.set(true);
    }

    public void pauseRefresher() {
        autoUpdate.set(false);
    }

    @Override
    public void close() {
        graphTable.getItems().clear();
        if (graphsRefresher != null && timer != null) {
            graphsRefresher.cancel();
            timer.cancel();
        }
    }
}
