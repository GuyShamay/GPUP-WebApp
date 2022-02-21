package admin.component.actions.circuit;

import admin.util.GraphUtil;
import admin.component.dashboard.AdminDashboardController;
import admin.util.Constants;
import admin.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static admin.util.Constants.GRAPH_NAME;
import static admin.util.Constants.TARGET_NAME;

public class CircuitController {
    @FXML
    private Label msgLabel;
    @FXML
    private ComboBox<String> targetsComboBox;
    @FXML
    private HBox resultHBox;

    private AdminDashboardController adminDashboardController;
    private final StringProperty msg;

    public CircuitController() {
        msg = new SimpleStringProperty("");
    }

    @FXML
    public void initialize() {
        msgLabel.textProperty().bind(msg);
    }

    @FXML
    void findButtonClicked(ActionEvent event) {
        msg.set("");
        resultHBox.getChildren().clear();
        if (validSelection()) {

            String graphName = adminDashboardController.getSelectedGraphName();
            findCircuit(graphName);
        } else {
            msg.set("Please select a target");
        }
    }

    private void findCircuit(String graphName) {
        String finalUrl = Objects.requireNonNull(HttpUrl.parse(Constants.FIND_CIRCUIT))
                .newBuilder()
                .addQueryParameter(GRAPH_NAME, graphName)
                .addQueryParameter(TARGET_NAME, targetsComboBox.getValue())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String targetsAsString = Objects.requireNonNull(response.body()).string();
                    System.out.println(targetsAsString);
                    JsonArray jsonArray = JsonParser.parseString(targetsAsString).getAsJsonArray();
                    List<String> list = GraphUtil.parseJsonArrayToStringList(jsonArray);
                    Platform.runLater(() -> {
                        for (int i = 0; i < list.size(); i++) {
                            Label label;
                            if (i == list.size() - 1) {
                                label = new Label(list.get(i));
                            } else {
                                label = new Label(list.get(i) + " -> ");
                            }
                            // label.getStyleClass().add("lblItem");
                            resultHBox.getChildren().add(label);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        try {
                            msg.set(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private boolean validSelection() {
        return !targetsComboBox.getSelectionModel().isEmpty();
    }

    public void fillTargetsComboBox() {
        GraphUtil.getGraphTargetsAsync(adminDashboardController.getSelectedGraphName(), this::updateTargets);
    }

    private void updateTargets(ObservableList<String> list) {
        targetsComboBox.setItems(list);
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

}
