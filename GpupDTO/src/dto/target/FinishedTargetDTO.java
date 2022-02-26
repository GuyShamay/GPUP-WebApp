package dto.target;

import dto.util.DTOUtil;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.time.Instant;

public class FinishedTargetDTO {

    private String name;
    private String executionName;
    private String logs;
    private String worker;
    private String processingTime;
    private FinishResultDTO finishResult;
    private String startingTime;

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    public FinishedTargetDTO(String name, String executionName, String logs, String worker, FinishResultDTO finishResult, String processingTime, String startingTime) {
        this.name = name;
        this.executionName = executionName;
        this.logs = logs;
        this.worker = worker;
        this.finishResult = finishResult;
        this.processingTime = processingTime;
        this.startingTime = startingTime;
    }

    public FinishedTargetDTO() {
    }

    public String getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutionName() {
        return executionName;
    }

    public void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public FinishResultDTO getFinishResult() {
        return finishResult;
    }

    public void setFinishResult(FinishResultDTO finishResult) {
        this.finishResult = finishResult;
    }
}
