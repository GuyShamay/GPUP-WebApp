package admin.component.tasks.config.simulation;

import admin.component.tasks.config.TaskConfigController;
import dto.execution.config.CompilationConfigDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class CompileConfigController {
    private TaskConfigController taskController;
    private CompilationConfigDTO compileConfig;
    private boolean validSrc;
    private boolean validDest;

    @FXML
    private Label srcLabel;
    @FXML
    private Label destLabel;
    @FXML
    private Button submitButton;

    @FXML
    void buttonDestClicked(ActionEvent event) {
        validDest = chooseDir(event, destLabel);
        if (validDest) {
            compileConfig.setDestDir(destLabel.getText());
        }
    }

    @FXML
    void buttonSrcClicked(ActionEvent event) {
        validSrc = chooseDir(event, srcLabel);
        if (validSrc) {
            compileConfig.setSrcDir(srcLabel.getText());
        }
    }

    private boolean chooseDir(ActionEvent event, Label label) {
        Node node = (Node) event.getSource();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDir =
                directoryChooser.showDialog(node.getScene().getWindow());

        if (selectedDir == null) {
            label.setText("No Directory selected");
            return false;
        } else {
            label.setText(selectedDir.getAbsolutePath());
            return true;
        }
    }

    @FXML
    void buttonSubmitClicked(ActionEvent event) {
        if (validDest && validSrc) {
            taskController.updateConfig(compileConfig);
            submitButton.setDisable(true);
            taskController.showFinalSubmit();
        } else {
            if (!validDest) {
                destLabel.setText("Please select folders");
            }
            if (!validSrc) {
                srcLabel.setText("Please select folders");
            }
        }
    }

    public void setTaskConfigController(TaskConfigController taskConfigController) {
        this.taskController = taskConfigController;
    }

    @FXML
    public void initialize() {
        compileConfig = new CompilationConfigDTO();
    }
}
