package admin.component.actions.path;

import admin.util.GraphUtil;
import admin.component.dashboard.AdminDashboardController;
import admin.util.Constants;
import admin.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dto.actions.FindPathsConfigDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static admin.util.Constants.*;

public class PathsController {
    @FXML
    private CheckBox checkBoxDependsOn;
    @FXML
    private CheckBox checkBoxRequiredFor;
    @FXML
    private ComboBox<String> comboBoxFrom;
    @FXML
    private ComboBox<String> comboBoxTo;
    @FXML
    private VBox vboxPath;
    @FXML
    private Label labelMessage;

    private AdminDashboardController adminDashboardController;
    private final StringProperty msg;

    public PathsController() {
        msg = new SimpleStringProperty("");
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
    public void initialize() {
        labelMessage.textProperty().bind(msg);
    }

    @FXML
    void buttonFindPathClicked(ActionEvent event) {
        vboxPath.getChildren().clear();
        if (checkBoxDependsOn.isSelected() || checkBoxRequiredFor.isSelected()) {
            if (isValidFromToChoose()) {                          ////// WHEN // COMBOBOX /////////
                findPaths(parseFindPath());                             ////// WILL // BE // FULL ///////
            } else {                                              /////////////////////////////////
                msg.set("Please select target (From, To)");       //////////////////////////////////
            }                                                     ////////////////////////////////
        } else {
            msg.set("Please select relation");
        }
    }

    private void findPaths(String findPathsAsString) {
        RequestBody body = RequestBody.create(findPathsAsString, MediaType.parse("application/json"));

        HttpClientUtil.runAsyncWithBody(Constants.FIND_PATHS, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> msg.set("Failing to find paths"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String pathsAsString = Objects.requireNonNull(response.body()).string();
                    JsonArray jsonArray = JsonParser.parseString(pathsAsString).getAsJsonArray();

                    List<String> paths = GraphUtil.parseJsonArrayToStringList(jsonArray); // there are paths (promise)
                    Platform.runLater(() -> {
                        for (String s : paths) {
                            Label label = new Label(s);
                            //label.getStyleClass().add("lblItem");
                            vboxPath.getChildren().add(label);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        try {
                            msg.set(Objects.requireNonNull(response.body()).string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });

    }

    private String parseFindPath() {
        String relationType = checkBoxDependsOn.isSelected() ? "DependsOn" : "RequiredFor";
        String from = comboBoxFrom.getValue();
        String to = comboBoxTo.getValue();
        //  String from = "B";
        //  String to = "E";

        FindPathsConfigDTO findPathDTO = new FindPathsConfigDTO(adminDashboardController.getSelectedGraphName(), from, to, relationType);
        return GSON_INST.toJson(findPathDTO);
    }

    private boolean isValidFromToChoose() {
        return (!comboBoxFrom.getSelectionModel().isEmpty() &&
                !comboBoxTo.getSelectionModel().isEmpty());
    }

    public void fillTargetsComboBox() {
        GraphUtil.getGraphTargetsAsync(adminDashboardController.getSelectedGraphName(), this::updateTargets);
    }

    private void updateTargets(ObservableList<String> list) {
        comboBoxFrom.setItems(list);
        comboBoxTo.setItems(list);
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

}