package dto.execution;

import dto.execution.config.ConfigDTO;
import dto.util.DTOUtil;
import engine.execution.Execution;

public class WorkerExecutionDTO {
    private String name;
    private int workersCount;
    private double progress;
    private int price;
    private DTOUtil.ExecutionTypeDTO type;
    private ConfigDTO executionDetails;

    public WorkerExecutionDTO(Execution execution) {
        this.name = execution.getName();
        this.workersCount = execution.getCurrentWorkersCount();
        this.price = execution.getPrice();
        this.progress = execution.getProgress();
        this.type = DTOUtil.ExecutionTypeDTO.valueOf(execution.getType().name());
        this.executionDetails = execution.getExecutionDetails();
    }

    public WorkerExecutionDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(int workersCount) {
        this.workersCount = workersCount;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public DTOUtil.ExecutionTypeDTO getType() {
        return type;
    }

    public void setType(DTOUtil.ExecutionTypeDTO type) {
        this.type = type;
    }

    public ConfigDTO getExecutionDetails() {
        return executionDetails;
    }

    public void setExecutionDetails(ConfigDTO executionDetails) {
        this.executionDetails = executionDetails;
    }
}
