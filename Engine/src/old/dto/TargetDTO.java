package old.dto;

import old.component.target.oldFinishResult;
import old.component.target.RunResult;
import old.component.target.oldTarget;
import old.component.target.TargetType;

import java.util.ArrayList;
import java.util.List;

public class TargetDTO {
    private final String name;
    private final String userData;
    private List<String> requiredForList;
    private List<String> dependsOnList;
    private final TargetType type;
    private final RunResult runResult;
    private final oldFinishResult finishResult;

    public TargetDTO(oldTarget target) {
        name = target.getName();
        type = target.getType();
        userData = target.getUserData();
        runResult = target.getRunResult();
        finishResult = target.getFinishResult();
        dependsOnList = updateList(target.getDependsOnList());
        requiredForList = updateList(target.getRequiredForList());
    }


    public String getName() {
        return name;
    }

    public List<String> getRequiredForList() {
        return requiredForList;
    }

    public List<String> getDependsOnList() {
        return dependsOnList;
    }

    public TargetType getType() {
        return type;
    }

    public RunResult getRunResult() {
        return runResult;
    }

    public oldFinishResult getFinishResult() {
        return finishResult;
    }

    private List<String> updateList(List<oldTarget> targetList) {
        List<String> list = new ArrayList<>();
        for (oldTarget t : targetList) {
            list.add(t.getName());
        }
        return list;
    }

    @Override
    public String toString() {
        int i = 0;
        StringBuilder str = new StringBuilder();
        str.append("Target's Name: " + getName());
        str.append("\nTarget's Type: " + getType());
        str.append("\nThis target depends on: ");
        str.append(printList(dependsOnList));
        str.append("\nThis target is required for: ");
        str.append(printList(requiredForList));
        str.append("\nThe target's data: ");
        if (userData == null) {
            str.append("No Data\n");
        } else {
            str.append(userData + "\n");
        }
        return str.toString();
    }


    private String printList(List<String> list) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        if (list.size() != 0) {
            for (String name : list) {
                if (i == 0) {
                    str.append(name);
                    i++;
                } else {
                    str.append(" , ").append(name);
                }
            }
        } else {
            str.append(" No targets at all");
        }
        return str.toString();
    }
}