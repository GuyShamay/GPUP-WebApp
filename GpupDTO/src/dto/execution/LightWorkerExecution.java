package dto.execution;

import engine.execution.Execution;

import java.util.List;

public class LightWorkerExecution {
    private double progress;
    private int workers;
    private String name;

    public LightWorkerExecution(Execution e) {
        this.name = e.getName();
        this.progress = e.getProgress();
        this.workers = e.getCurrentWorkersCount();
    }

    public LightWorkerExecution() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
}
