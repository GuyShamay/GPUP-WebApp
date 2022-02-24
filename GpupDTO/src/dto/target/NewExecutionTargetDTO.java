package dto.target;

import engine.target.Target;

public class NewExecutionTargetDTO {
    private String name;
    private String userData;

    public NewExecutionTargetDTO(Target target) {
        this.name = target.getName();
        this.userData = target.getUserData();
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
}
