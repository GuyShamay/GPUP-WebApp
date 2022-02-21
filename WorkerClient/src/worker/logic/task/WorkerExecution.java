package worker.logic.task;


import dto.execution.config.ConfigDTO;

public class WorkerExecution {
    private String name;
    private int workersCount;
    private int progress;
    private int credit;
    private int price;
    private int doneTargets;
    private ExecutionType type;
    private WorkerExecutionStatus executionStatus;
    private ConfigDTO executionDetails;

    public WorkerExecution() {
        executionStatus = WorkerExecutionStatus.Registered;
        credit = 0;
        doneTargets = 0;
    }

    public ConfigDTO getExecutionDetails() {
        return executionDetails;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setExecutionDetails(ConfigDTO executionDetails) {
        this.executionDetails = executionDetails;
    }

    public void pause() {
        this.executionStatus = WorkerExecutionStatus.Paused;
    }

    public void activate() {
        this.executionStatus = WorkerExecutionStatus.Active;
    }

    public void stop() {
        this.executionStatus = WorkerExecutionStatus.Unregistered;
    }

    public WorkerExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public ExecutionType getType() {
        return type;
    }

    public void setType(ExecutionType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCredit() {
        return credit;
    }

    public synchronized void addCredit() {
        this.credit += price;
    }

    public int getDoneTargets() {
        return doneTargets;
    }

    public synchronized void addDoneTarget() {
        this.doneTargets++;
    }

    public int getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(int workersCount) {
        this.workersCount = workersCount;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
