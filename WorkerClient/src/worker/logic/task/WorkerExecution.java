package worker.logic.task;


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
        initTask();
    }

    private void initTask() {
        switch (this.type){
            case Simulation:
                task= new SimulationTask((SimulationConfigDTO) executionDetails);
                break;
            case Compilation:
                //  CompilationTask.run(target.getName(),)
                break;
        }
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

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public int getPrice() {
        return price;
    }

    public void runTaskTarget(TaskTarget target) throws InterruptedException {
        System.out.println(target.getName() + " / " + target.getExecutionName() + ": DONE");

        /// implement task - compilation and simulation

        task.run(target);
        FinishedTargetDTO finishedTarget = new FinishedTargetDTO(target.getName(), target.getExecutionName(), target.getLogs(), this.name, FinishResultDTO.valueOf(target.getStatus().toString()));
        System.out.println("---------------------------------------------------------"+finishedTarget.toString());
        String finishedTargetAsString = GSON_INST.toJson(finishedTarget);
        RequestBody body = RequestBody.create(finishedTargetAsString, MediaType.parse("application/json"));

        HttpClientUtil.runAsyncWithBody(Constants.SEND_TARGET, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    public Task getTask() {
        return task;
    }
}