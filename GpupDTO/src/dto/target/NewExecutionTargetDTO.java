package dto.target;

import engine.target.Target;

public class NewExecutionTargetDTO {
    private String name;
    private String userData;
    private String executionName;

    public NewExecutionTargetDTO(Target target,String executionName) {
        this.name = target.getName();
        this.userData = target.getUserData();
        this.executionName=executionName;
    }

    public NewExecutionTargetDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getExecutionName() {
        return executionName;
    }

    public void setExecutionName(String executionName) {
        this.executionName = executionName;
    }
}
