package dto.target;

import engine.target.FinishResult;
import engine.target.RunResult;
import engine.target.TargetType;
import engine.target.Target;
import javafx.beans.property.SimpleIntegerProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TargetDTO {
    private String name;
    private String userData;
    private List<String> requiredForList;
    private int requiredForCount;
    private List<String> dependsOnList;
    private int dependsOnCount;
    private TargetTypeDTO type;
    private RunResultDTO runResult;
    private FinishResultDTO finishResult;
    //Instance & Duration
    private Instant startRunningTime;
    private Instant startWaitingTime;
    private Duration taskRunDuration;

    private List<String> dependsOnToOpenList;
    private List<String> skippedBecauseList;

    public TargetDTO(Target target) {
        initBasicData(target);
        initTypesAndResults(target);
        initTimes(target);
        dependsOnToOpenList= new ArrayList<>();
        skippedBecauseList= new ArrayList<>();
    }

    public TargetDTO() {
        startRunningTime = null;
        startWaitingTime = null;
        taskRunDuration = null;
    }

    private void initTimes(Target target) {
        startRunningTime = target.getStartRunningTime();
        startWaitingTime = target.getStartWaitingTime();
        taskRunDuration = target.getTaskRunDuration();
    }

    private void initTypesAndResults(Target target) {
        this.type = TargetTypeDTO.valueOf(target.getType().toString());
        this.runResult = RunResultDTO.valueOf(target.getRunResult().toString());
        if (target.getFinishResult() != null) {
            this.finishResult = FinishResultDTO.valueOf(target.getFinishResult().toString());
        } else {
            finishResult = null;
        }
    }

    private void initBasicData(Target target) {
        requiredForList = new ArrayList<>();
        dependsOnList = new ArrayList<>();

        this.name = target.getName();
        this.userData = target.getUserData();
        target.getDependsOnList().forEach(t -> this.dependsOnList.add(t.getName()));
        this.dependsOnCount = this.dependsOnList.size();
        target.getRequiredForList().forEach(t -> this.requiredForList.add(t.getName()));
        this.requiredForCount = this.requiredForList.size();
    }

    public String getName() {
        return name;
    }

    public String getUserData() {
        return userData;
    }

    public int getRequiredForCount() {
        return requiredForCount;
    }

    public int getDependsOnCount() {
        return dependsOnCount;
    }

    public List<String> getRequiredForList() {
        return requiredForList;
    }

    public List<String> getDependsOnList() {
        return dependsOnList;
    }

    public TargetTypeDTO getType() {
        return type;
    }

    public RunResultDTO getRunResult() {
        return runResult;
    }

    public FinishResultDTO getFinishResult() {
        return finishResult;
    }

    public Instant getStartRunningTime() {
        return startRunningTime;
    }

    public Instant getStartWaitingTime() {
        return startWaitingTime;
    }

    public Duration getTaskRunDuration() {
        return taskRunDuration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public void setRequiredForList(List<String> requiredForList) {
        this.requiredForList = requiredForList;
        this.requiredForCount = this.requiredForList.size();

    }

    public void setDependsOnList(List<String> dependsOnList) {
        this.dependsOnList = dependsOnList;
        this.dependsOnCount = this.dependsOnList.size();
    }

    public void setType(TargetTypeDTO type) {
        this.type = type;
    }

    public void setRunResult(RunResultDTO runResult) {
        this.runResult = runResult;
    }

    public void setFinishResult(FinishResultDTO finishResult) {
        this.finishResult = finishResult;
    }

    public void setStartRunningTime(Instant startRunningTime) {
        this.startRunningTime = startRunningTime;
    }

    public void setStartWaitingTime(Instant startWaitingTime) {
        this.startWaitingTime = startWaitingTime;
    }

    public void setTaskRunDuration(Duration taskRunDuration) {
        this.taskRunDuration = taskRunDuration;
    }

    public void initRunningFields(Target target) {
        dependsOnToOpenList = target.getDependsOnToOpenList();
        skippedBecauseList = target.getSkippedBecauseList();
    }

    public void setSkippedBecauseList(List<String> skippedBecauseList) {
        this.skippedBecauseList=skippedBecauseList;
    }

    public void setDependsOnToOpenList(List<String> dependsOnToOpenList) {
        this.dependsOnToOpenList=dependsOnToOpenList;
    }

    public List<String> getDependsOnToOpenList() {
        return dependsOnToOpenList;
    }

    public List<String> getSkippedBecauseList() {
        return skippedBecauseList;
    }

    public Duration getWaitingTimeInMs() {
        return Duration.ZERO;
    }

    public Duration getProcessingTimeInMs() {
        return Duration.ZERO;
    }
}
