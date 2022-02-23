package dto.execution;

import dto.execution.config.ConfigDTO;
import dto.graph.GraphDTO;
import dto.util.DTOUtil;
import engine.execution.Execution;

import java.util.List;

public class ExecutionDTO {
    private GraphDTO graphDTO;
    private String name;
    private String creatingUser;
    private String status;
    private int workersCount;
    private int price;
    private double progress;
    private DTOUtil.ExecutionTypeDTO type;
    private List<String> workers; // ????
    private ConfigDTO config;

    public ExecutionDTO(Execution execution) {
        this.name = execution.getName();
        this.creatingUser = execution.getCreatingUser();
        this.status = execution.getStatus().name();
        this.graphDTO = new GraphDTO(execution.getTaskGraph());
        this.workersCount = execution.getCurrentWorkersCount();
        this.price = execution.getPrice();
        this.type = DTOUtil.ExecutionTypeDTO.valueOf(execution.getType().name());
        this.workers = execution.getWorkers();
        this.progress = execution.getProgress();
        this.config = execution.getExecutionDetails();
    }

    public ExecutionDTO() {
    }

    public void setConfig(ConfigDTO config) {
        this.config = config;
    }

    public ConfigDTO getConfig() {
        return config;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public List<String> getWorkers() {
        return workers;
    }

    public void setWorkers(List<String> workers) {
        this.workers = workers;
    }

    public DTOUtil.ExecutionTypeDTO getType() {
        return type;
    }

    public void setType(DTOUtil.ExecutionTypeDTO type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatingUser() {
        return creatingUser;
    }

    public void setCreatingUser(String creatingUser) {
        this.creatingUser = creatingUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GraphDTO getGraphDTO() {
        return graphDTO;
    }

    public void setGraphDTO(GraphDTO graphDTO) {
        this.graphDTO = graphDTO;
    }

    public int getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(int workersCount) {
        this.workersCount = workersCount;
    }
}
