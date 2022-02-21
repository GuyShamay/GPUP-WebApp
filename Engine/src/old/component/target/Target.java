package old.component.target;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Target {

    private final String name;
    private String userData;
    private List<Target> requiredForList;
    private List<Target> dependsOnList;
    private TargetType type;
    private RunResult runResult;
    private FinishResult finishResult;
    private List<Target> justOpenedList = new ArrayList<>();
    private List<Target> skippedList = new ArrayList<>();
    private Duration taskRunDuration;
    private int serialSetCounter;
    private boolean isLockBySerialSet;
    private Instant startRunningTime;
    private Instant startWaitingTime;
    private List<String> serialSets;

    public Target(String name) {
        this.name = name;
        requiredForList = new ArrayList<>();
        dependsOnList = new ArrayList<>();
        runResult = RunResult.FROZEN;
        serialSets = new ArrayList<>();
    }

    public String getName() {
        return name;
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

    public List<Target> getJustOpenedList() {
        return justOpenedList;
    }

    public List<Target> getDependsOnList() {
        return dependsOnList;
    }

    public String getUserData() {
        return userData;
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

    //GPUP1 func
    public boolean isAllAdjFinished() {
        return dependsOnList.stream().allMatch(target -> (target.getRunResult().equals(RunResult.FINISHED)));
    }

    //GPUP1 func
    public boolean isAllAdjFinishedWithoutFailure() {
        if (isAllAdjFinished()) {
            return dependsOnList.stream().allMatch(target -> (target.getFinishResult().equals(FinishResult.SUCCESS) || target.getFinishResult().equals(FinishResult.WARNING)));
        } else {
            return false;
        }
    }

    //GPUP1 func
    public void addDependOnTarget(Target target) {
        if (!dependsOnList.contains(target))
            dependsOnList.add(target);
    }

    //GPUP1 func
    public void addRequiredForTarget(Target target) {
        if (!requiredForList.contains(target)) {
            requiredForList.add(target);
        }
    }

    //GPUP1 func
    public boolean isDependency(Target target, String type) {
        if (type.equals("dependsOn")) {
            return dependsOnList.contains(target);
        } else {
            return requiredForList.contains(target);
        }
    }

    public List<Target> getSkippedList() {
        return skippedList;
    }

    public void addToJustOpenedList(Target target) {
        justOpenedList.add(target);
    }

    public void clearHelpingLists() {
        justOpenedList.clear();
        skippedList.clear();
    }

    public Duration getTaskRunDuration() {
        return taskRunDuration;
    }

    public int getSerialSetCounter() {
        return serialSetCounter;
    }

    public void incrementSerialSetCounter() {
        serialSetCounter++;
    }

    public void setSerialSetCounter(int serialSetCounter) {
        this.serialSetCounter = serialSetCounter;
    }

    public boolean isLock() {
        return isLockBySerialSet;
    }

    public void lock() {
        isLockBySerialSet = true;
    }

    public void unlock() {
        isLockBySerialSet = false;
    }

    public void setRequiredFor(List<Target> requiredFor) {
        requiredForList = requiredFor;
    }

    public void setDependOn(List<Target> dependOn) {
        dependsOnList = dependOn;
    }

    public void setStartRunningTime() {
        startRunningTime = Instant.now();
    }

    public void setStartWaitingTime() {
        startWaitingTime = Instant.now();
    }

    public Duration getWaitingTimeDuration() {
        if(runResult.equals(RunResult.WAITING))
            return Duration.between(startWaitingTime,Instant.now());
        return null;
    }

    public Duration getProcessingTimeDuration() {
        if(runResult.equals(RunResult.INPROCESS))
            return Duration.between(startRunningTime,Instant.now());
        return null;
    }

    public List<Target> getDependsOnToOpenList() {
        final List<Target> res = new ArrayList<>();
        dependsOnList.forEach(target -> {
            if(target.getFinishResult()==null)
                res.add(target);
        });

        return res;
    }

    public List<Target> getSkippedBecauseList() {
        final List<Target> res = new ArrayList<>();
        dependsOnList.forEach(target -> {
            if(target.getFinishResult()==FinishResult.FAILURE)
                res.add(target);
        });

        return res;
    }

    public List<String> getSerialSets() {
        return serialSets;
    }

    public void addToSerialSetsList(String name) {
        serialSets.add(name);
    }
}
