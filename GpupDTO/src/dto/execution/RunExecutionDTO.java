package dto.execution;

import engine.execution.Execution;
import engine.progressdata.ProgressData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class RunExecutionDTO {
    private String status;
    private Double progress;
    // Get from Execution's ProgressData:
    private ObservableList<String> frozen;
    private ObservableList<String> waiting;
    private ObservableList<String> inProcess;
    private ObservableList<String> skipped;
    private ObservableList<String> failure;
    private ObservableList<String> success;
    private ObservableList<String> warnings;

    public RunExecutionDTO(Execution execution) {
        ProgressData progressData = execution.getProgressData();
        this.status = execution.getStatus().name();
        this.progress = execution.getProgress();

        this.frozen = FXCollections.observableArrayList(progressData.getFrozen());
        this.waiting = FXCollections.observableArrayList(progressData.getWaiting());
        this.inProcess = FXCollections.observableArrayList(progressData.getInprocces());
        this.skipped = FXCollections.observableArrayList(progressData.getSkipped());
        this.failure = FXCollections.observableArrayList(progressData.getFailure());
        this.success = FXCollections.observableArrayList(progressData.getSuccess());
        this.warnings = FXCollections.observableArrayList(progressData.getWarning());
    }

    public RunExecutionDTO() {
    }

    // Setters and Getters:
    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ObservableList<String> getFrozen() {
        return frozen;
    }

    public void setFrozen(List<String> frozen) {
        this.frozen = FXCollections.observableArrayList(frozen);
    }

    public ObservableList<String> getWaiting() {
        return waiting;
    }

    public void setWaiting(List<String> waiting) {
        this.waiting = FXCollections.observableArrayList(waiting);
    }

    public ObservableList<String> getInProcess() {
        return inProcess;
    }

    public void setInProcess(List<String> inProcess) {
        this.inProcess = FXCollections.observableArrayList(inProcess);
    }

    public ObservableList<String> getSkipped() {
        return skipped;
    }

    public void setSkipped(List<String> skipped) {
        this.skipped = FXCollections.observableArrayList(skipped);
    }

    public ObservableList<String> getFailure() {
        return failure;
    }

    public void setFailure(List<String> failure) {
        this.failure = FXCollections.observableArrayList(failure);
    }

    public ObservableList<String> getSuccess() {
        return success;
    }

    public void setSuccess(List<String> success) {
        this.success = FXCollections.observableArrayList(success);
    }

    public ObservableList<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = FXCollections.observableArrayList(warnings);
    }
}
