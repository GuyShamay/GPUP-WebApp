package engine.target;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Target implements Cloneable {
    private final String name;
    private String userData;
    private List<Target> requiredForList;
    private List<Target> dependsOnList;
    private List<Target> justOpenedList;
    private List<Target> skippedList;

    private TargetType type;
    private RunResult runResult;
    private FinishResult finishResult;

    //Instance & Duration
    private Instant startRunningTime;
    private Instant startWaitingTime;
    private Duration taskRunDuration;

    public Target(String name) {
        this.name = name;
        requiredForList = new ArrayList<>();
        dependsOnList = new ArrayList<>();
        justOpenedList = new ArrayList<>();
        skippedList = new ArrayList<>();
        runResult = RunResult.FROZEN;
    }

    public String getName() {
        return name;
    }

    public Instant getStartRunningTime() {
        return startRunningTime;
    }

    public Instant getStartWaitingTime() {
        return startWaitingTime;
    }

    public TargetType getType() {
        return type;
    }

    public RunResult getRunResult() {
        return runResult;
    }

    public FinishResult getFinishResult() {
        return finishResult;
    }

    public List<Target> getRequiredForList() {
        return requiredForList;
    }

    public List<Target> getDependsOnList() {
        return dependsOnList;
    }

    public List<Target> getJustOpenedList() {
        return justOpenedList;
    }

    public List<Target> getSkippedList() {
        return skippedList;
    }

    public String getUserData() {
        return userData;
    }

    public Duration getTaskRunDuration() {
        return taskRunDuration;
    }

    public Duration getWaitingTime() {
        if (runResult.equals(RunResult.WAITING))
            return Duration.between(startWaitingTime, Instant.now());
        return null;
    }

    public Duration getProcessingTime() {
        if (runResult.equals(RunResult.INPROCESS))
            return Duration.between(startRunningTime, Instant.now());
        return null;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public void setTaskRunDuration(Duration taskRunDuration) {
        this.taskRunDuration = taskRunDuration;
    }

    public void setRunResult(RunResult runResult) {
        this.runResult = runResult;
    }

    public void setFinishResult(FinishResult finishResult) {
        this.finishResult = finishResult;
    }

    public void setType(TargetType type) {
        this.type = type;
    }

    public void setRequiredForList(List<Target> requiredFor) {
        requiredForList = requiredFor;
    }

    public void setDependsOnList(List<Target> dependOn) {
        dependsOnList = dependOn;
    }

    public void startRunningTime() {
        startRunningTime = Instant.now();
    }

    public void startWaitingTime() {
        startWaitingTime = Instant.now();
    }

    public void addDependOnTarget(Target target) {
        if (!dependsOnList.contains(target))
            dependsOnList.add(target);
    }

    public void addRequiredForTarget(Target target) {
        if (!requiredForList.contains(target)) {
            requiredForList.add(target);
        }
    }

    public boolean isDependency(Target target, String type) {
        if (type.equals("dependsOn")) {
            return dependsOnList.contains(target);
        } else {
            return requiredForList.contains(target);
        }
    }

    @Override
    public Target clone() throws CloneNotSupportedException {
        Target newTarget = new Target(this.name);
        newTarget.setUserData(this.userData);
        newTarget.setType(this.type);
        newTarget.setRunResult(this.runResult);
        newTarget.setFinishResult(this.finishResult);
        newTarget.setTaskRunDuration(this.taskRunDuration);
        newTarget.setStartRunningTime(this.startRunningTime);
        newTarget.setStartWaitingTime(this.startWaitingTime);
        return newTarget;
    }

    public void clearHelpingLists() {
        justOpenedList.clear();
        skippedList.clear();
    }

    public void setStartRunningTime(Instant startRunningTime) {
        this.startRunningTime = startRunningTime;
    }

    public void setStartWaitingTime(Instant startWaitingTime) {
        this.startWaitingTime = startWaitingTime;
    }

    public String getNameAndWorker() {
        return name + " - " + "Test"; // NEED TO UPDATE: WORKER'S NAME
    }
}
