package dto.target;

import dto.util.DTOUtil;

public class FinishedTargetDTO {

    private String name;
    private String executionName;
    private String logs;
    private String worker;
    private FinishResultDTO finishResult;

    public FinishedTargetDTO(String name, String executionName, String logs, String worker, FinishResultDTO finishResult) {
        this.name = name;
        this.executionName = executionName;
        this.logs = logs;
        this.worker = worker;
        this.finishResult = finishResult;
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
