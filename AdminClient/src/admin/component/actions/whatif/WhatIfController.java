package admin.component.actions.whatif;

import admin.component.dashboard.AdminDashboardController;
import admin.util.Constants;
import admin.util.GraphUtil;
import admin.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dto.target.TargetDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static admin.util.Constants.*;

public class WhatIfController {
    @FXML
    private CheckBox checkBoxDependsOn;
    @FXML
    private CheckBox checkBoxRequiredFor;
    @FXML
    private ComboBox<String> targetsComboBox;
    @FXML
    private Label labelMessage;
    @FXML
    private HBox resultHBox;

    private AdminDashboardController adminDashboardController;
    private final StringProperty msg;


    public WhatIfController() {
        msg = new SimpleStringProperty("");
    }

    @FXML
    public void initialize() {
        labelMessage.textProperty().bind(msg);
    }

    @FXML
    void checkBoxDependsOnChosen(ActionEvent event) {
        checkBoxRequiredFor.setSelected(!checkBoxDependsOn.isSelected());
    }

    @FXML
    void checkBoxRequiredForChosen(ActionEvent event) {
        checkBoxDependsOn.setSelected(!checkBoxRequiredFor.isSelected());
    }

    @FXML
    void buttonSubmitClicked(ActionEvent event) {
        resultHBox.getChildren().clear();
        if (checkBoxDependsOn.isSelected() || checkBoxRequiredFor.isSelected()) {
            if (isValidChosenTarget()) {
                whatif(adminDashboardController.getSelectedGraphName());
            } else {
                msg.set("Please select target (From, To)");
            }
        } else {
            msg.set("Please select relation");
        }
    }

    private void whatif(String graphName) {
        String finalUrl = Objects.requireNonNull(HttpUrl.parse(Constants.WHAT_IF))
                .newBuilder()
                .addQueryParameter(GRAPH_NAME, graphName)
                .addQueryParameter(TARGET_NAME, targetsComboBox.getValue())
                .addQueryParameter(RELATION_TYPE, checkBoxDependsOn.isSelected() ? "DependsOn" : "RequiredFor")
                .build()
                .toString();
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> resultHBox.getChildren().add(new Label("Failing to find paths")));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String targetAjJson = Objects.requireNonNull(response.body()).string();
                    JsonArray jsonArray = JsonParser.parseString(targetAjJson).getAsJsonArray();
                    List<TargetDTO> targets = GraphUtil.parseJsonArrayToTargetDTOList(jsonArray);
                    Platform.runLater(() -> {
                        for (int i = 0; i < targets.size(); i++) {
                            Label label;
                            if (i == targets.size() - 1) {
                                label = new Label(targets.get(i).getName());
                            } else {
                                label = new Label(targets.get(i).getName() + ", ");
                            }
                            //label.getStyleClass().add("lblItemTarget");
                            resultHBox.getChildren().add(label);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        try {
                            resultHBox.getChildren().add(new Label(response.body().string()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private boolean isValidChosenTarget() {
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
