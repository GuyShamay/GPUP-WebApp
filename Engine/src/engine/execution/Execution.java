package engine.execution;

import dto.execution.config.ConfigDTO;
import dto.execution.config.ExecutionConfigDTO;
import engine.graph.TargetGraph;

import java.util.ArrayList;
import java.util.List;

public class Execution {
    private TargetGraph taskGraph;
    private String name;
    private String creatingUser;
    private int currentWorkersCount;
    private int price;
    private int progress;
    private ExecutionStatus status;
    private ExecutionType type;
    private final List<String> workers;
    private ConfigDTO executionDetails;

    public Execution() {
        status = ExecutionStatus.New;
        currentWorkersCount = 0;
        progress = 0;
        workers = new ArrayList<>();
    }


    // -----------------------------------------------------------------------------

    public ConfigDTO getExecutionDetails() {
        return executionDetails;
    }

    public void setExecutionDetails(ConfigDTO executionDetails) {
        this.executionDetails = executionDetails;
    }

    public int getProgress() {
        return 100 * (progress / taskGraph.getCount());
    }

    public ExecutionType getType() {
        return type;
    }

    public void setType(ExecutionType type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public TargetGraph getTaskGraph() {
        return taskGraph;
    }

    public void setTaskGraph(TargetGraph taskGraph) {
        this.taskGraph = taskGraph;
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

    public int getCurrentWorkersCount() {
        return currentWorkersCount;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public List<String> getWorkers() {
        return workers;
    }

    //----------------------------------------------------------------------------------

    public void createTaskGraph(ExecutionConfigDTO execConfig, TargetGraph baseGraph) {
        this.taskGraph = baseGraph.clone(execConfig);
    }

    public void createTaskGraph(TargetGraph baseGraph) {
        this.taskGraph = baseGraph.clone();
    }

    public void fromScratchReset() {
        this.taskGraph.fromScratchReset();
    }

    public synchronized void addWorker(String name) {
        workers.add(name);
        currentWorkersCount++;
    }

    public synchronized void removeWorker(String name) {
        workers.remove(name);
        currentWorkersCount--;
    }

    public boolean isWorkerExist(String name) {
        return workers.contains(name);
    }

    public synchronized void IncrementProgress() {
        if (progress < taskGraph.getCount()) {
            progress++;
        }
    }

    public void checkValidIncremental() {
        // if incremental chosen, and all targets are finished -> from scratch
        taskGraph.checkValidIncremental();
    }

    public boolean isAllowedToRegister() {
        return status == ExecutionStatus.New || status == ExecutionStatus.Paused || status == ExecutionStatus.Active;
    }
}
