package worker.logic.task;


import dto.execution.config.ConfigDTO;
import dto.target.NewExecutionTargetDTO;
import worker.logic.Worker;
import worker.logic.target.TaskTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import static worker.client.util.Constants.REFRESH_RATE;
import static worker.client.util.Constants.TARGET_REQ_REFRESH_RATE;

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
    private TargetsRequestRefresher refresher;
    private Consumer<List<TaskTarget>> consumer;

    public WorkerExecution() {
        executionStatus = WorkerExecutionStatus.Registered;
        credit = 0;
        doneTargets = 0;
        startRefresher();
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

    public void startRefresher() {
    //    refresher = new TargetsRequestRefresher(this.name, this::acceptTargets, this::EC);
        new Timer().schedule(refresher, TARGET_REQ_REFRESH_RATE, TARGET_REQ_REFRESH_RATE);
    }

    public void acceptTargets(List<NewExecutionTargetDTO> newTargets) {
        List<TaskTarget> list = new ArrayList<>();
        newTargets.forEach(t -> {
            TaskTarget target = new TaskTarget(t);
            target.setExecutionName(this.name);
            target.setType(this.type);
            target.setPayedPrice(this.price);
            // maybe add configDTO
            list.add(target);
        });
        consumer.accept(list);
    }


    private void EC(String s) {
        System.out.println(s);
    }

    public void pauseRequest() {
        refresher.pause();
    }

    public void resumeRequest() {
        refresher.resume();
    }

    public void setNewTargetsConsumer(Consumer<List<TaskTarget>> targets) {
        this.consumer = targets;
    }

    public int getPrice() {
        return price;
    }
}
