package admin.component.tasks.config.simulation;


import admin.component.tasks.config.TaskConfigController;
import dto.execution.config.SimulationConfigDTO;
import dto.util.DTOUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SimulationConfigController{
    private TaskConfigController taskController;
    private SimulationConfigDTO simulationConfig;

    @FXML
    private TextField procTimeTextField;
    @FXML
    private CheckBox randomCheckBox;

    @FXML
    private Spinner<Integer> successSpinner;

    @FXML
    private Spinner<Integer> warningSpinner;

    @FXML
    private Button submitButton;

    @FXML
    private Label warningLabel;

    @FXML
    void buttonSubmitClicked(ActionEvent event) {
        try {
            if (validProcTime()) {
                simulationConfig.setProcessingTime(Integer.parseInt(procTimeTextField.getText()));
                simulationConfig.setIsRandom(randomCheckBox.isSelected());
                simulationConfig.setSuccessProb((float) successSpinner.getValue() / 100);
                simulationConfig.setSuccessWithWarningsProb((float) warningSpinner.getValue() / 100);
                warningLabel.setText("Success!");
                taskController.updateConfig(simulationConfig);
                submitButton.setDisable(true);
                taskController.showFinalSubmit();
            }
        } catch (NumberFormatException ex) {
            warningLabel.setText("Processing Time must be Integer Number");
            procTimeTextField.setText("");
        }
    }

    public void setTaskConfigController(TaskConfigController taskConfigController) {
        this.taskController = taskConfigController;
    }

    private boolean validProcTime() {
        Integer.parseInt(procTimeTextField.getText());
        return true;
    }

    @FXML
    public void initialize() {
        simulationConfig = new SimulationConfigDTO();
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100);
        SpinnerValueFactory<Integer> factory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100);
        successSpinner.setValueFactory(factory);
        warningSpinner.setValueFactory(factory2);
        successSpinner.getValueFactory().setValue(50);
        warningSpinner.getValueFactory().setValue(50);
        setSpinnerOnEdit(warningSpinner);
        setSpinnerOnEdit(successSpinner);
    }

    private void setSpinnerOnEdit(Spinner<Integer> spinner) {
        spinner.setEditable(true);
        spinner.getEditor().textProperty().addListener((obs, oldval, newval) -> {
            try {
                Integer val = Integer.parseInt(newval);
                if (val >= 1 && val <= 100) {
                    spinner.getValueFactory().setValue(val);
                } else if (val > 100) {
                    spinner.getValueFactory().setValue(100);
                } else {
                    spinner.getValueFactory().setValue(1);
                }
            } catch (NumberFormatException ex) {
                spinner.getValueFactory().setValue(2);
            }
        });
    }
}
