package worker.logic.target;

import dto.target.NewExecutionTargetDTO;
import worker.logic.task.ExecutionType;

public class TaskTarget {
    private String name;
    private String userData;
    private String ExecutionName;
    private String logs;
    private Integer payedPrice;
    private ExecutionType type;
    private TargetStatus status;

    public TaskTarget() {
        payedPrice = null;
    }

    public TaskTarget(NewExecutionTargetDTO t) {
        this.name = t.getName();
        this.userData = t.getUserData();
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutionName() {
        return ExecutionName;
    }

    public void setExecutionName(String executionName) {
        ExecutionName = executionName;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public Integer getPayedPrice() {
        return payedPrice;
    }

    public void setPayedPrice(Integer payedPrice) {
        this.payedPrice = payedPrice;
    }

    public ExecutionType getType() {
        return type;
    }

    public void setType(ExecutionType type) {
        this.type = type;
    }

    public TargetStatus getStatus() {
        return status;
    }

    public void setStatus(TargetStatus status) {
        this.status = status;
    }
}
