package old.dto;

import old.component.target.oldTarget;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TargetInfoDTO {

    private final SimpleIntegerProperty dependsOn;
    private final SimpleIntegerProperty requiredFor;
    private final SimpleIntegerProperty serialSets;
    private final SimpleStringProperty name;
    private final SimpleStringProperty data;
    private final SimpleStringProperty type;
    private final List<String> dependsOnToOpenList;
    private final List<String> skippedBecauseList;
    private final List<String> serialSetsList;
    private final Duration waitingTimeInMs;
    private final Duration processingTimeInMs;
    private final String finishResult;
    private final String runResult;


    public TargetInfoDTO(oldTarget target) {
        data = new SimpleStringProperty(target.getUserData());
        name = new SimpleStringProperty(target.getName());
        type = new SimpleStringProperty(target.getType().toString());
        dependsOn = new SimpleIntegerProperty(target.getDependsOnList().size());
        requiredFor = new SimpleIntegerProperty(target.getRequiredForList().size());
        serialSets = new SimpleIntegerProperty(target.getSerialSetCounter());
        dependsOnToOpenList = convertToStringsList(target.getDependsOnToOpenList());
        skippedBecauseList = convertToStringsList(target.getSkippedBecauseList());
        waitingTimeInMs = target.getWaitingTimeDuration();
        processingTimeInMs = target.getProcessingTimeDuration();
        finishResult = target.getFinishResult() == null ? null : target.getFinishResult().toString();
        runResult = target.getRunResult().toString();
        serialSetsList = target.getSerialSets();

    }

    private List<String> convertToStringsList(List<oldTarget> list) {
        final List<String> res = new ArrayList<>();
        list.forEach(target -> res.add(target.getName()));
        return res;
    }
    public List<String> getDependsOnToOpenList() {
        return dependsOnToOpenList;
    }

    public List<String> getSkippedBecauseList() {
        return skippedBecauseList;
    }

    public Duration getWaitingTimeInMs() {
        return waitingTimeInMs;
    }

    public Duration getProcessingTimeInMs() {
        return processingTimeInMs;
    }

    public String getFinishResult() {
        return finishResult;
    }

    public String getRunResult() {
        return runResult;
    }

    public List<String> getSerialSetsList() {
        return serialSetsList;
    }
    public int getDependsOn() {
        return dependsOn.get();
    }

    public SimpleIntegerProperty dependsOnProperty() {
        return dependsOn;
    }

    public int getRequiredFor() {
        return requiredFor.get();
    }

    public SimpleIntegerProperty requiredForProperty() {
        return requiredFor;
    }

    public int getSerialSets() {
        return serialSets.get();
    }

    public SimpleIntegerProperty serialSetsProperty() {
        return serialSets;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getData() {
        return data.get();
    }

    public SimpleStringProperty dataProperty() {
        return data;
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }


}
