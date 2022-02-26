package worker.logic.task;


import dto.execution.config.CompilationConfigDTO;
import dto.execution.config.ConfigDTO;
import dto.execution.config.SimulationConfigDTO;
import dto.target.FinishResultDTO;
import dto.target.FinishedTargetDTO;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.logic.target.TaskTarget;

import java.io.IOException;

import static worker.client.util.Constants.*;

public class WorkerExecution {
    private String name;
    private int workersCount;
    private double progress;
    private int credit;
    private int price;
    private int doneTargets;
    private ExecutionType type;
    private WorkerExecutionStatus executionStatus;
    private ConfigDTO executionDetails;
    private Task task;


    public WorkerExecution() {
        executionStatus = WorkerExecutionStatus.Active;
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
        initTask();
    }

    private void initTask() {
        switch (this.type) {
            case Simulation:
                task = new SimulationTask((SimulationConfigDTO) executionDetails);
                break;
            case Compilation:
                task = new CompilationTask((CompilationConfigDTO) executionDetails);
                break;
        }
    }

    public void pause() {
        this.executionStatus = WorkerExecutionStatus.Paused;
    }

    public void activate() {
        this.executionStatus = WorkerExecutionStatus.Active;
    }

    //NEEDED?
    public void stop() {
        // this.executionStatus = WorkerExecutionStatus.Unregistered;
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

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public int getPrice() {
        return price;
    }

    public Task getTask() {
        return task;
    }

    public void setExecutionStatus(WorkerExecutionStatus status) {
        executionStatus = status;
    }
}