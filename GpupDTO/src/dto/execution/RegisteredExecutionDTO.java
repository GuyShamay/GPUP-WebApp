package dto.execution;

import engine.execution.Execution;

public class RegisteredExecutionDTO {
    private String name;
    private Double progress;
    private int workers;
    private int credit;
    private int doneTargets;

    public RegisteredExecutionDTO(Execution execution) {
        this.name = execution.getName();
        this.progress = execution.getProgress();
        this.workers = execution.getCurrentWorkersCount();
        this.credit = 0;
        this.doneTargets = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getDoneTargets() {
        return doneTargets;
    }

    public void setDoneTargets(int doneTargets) {
        this.doneTargets = doneTargets;
    }
}
