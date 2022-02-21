package old.dto;

import old.component.target.FinishResult;
import old.component.target.Target;

import java.util.List;

public class ProcessedTargetDTO implements GPUPConsumer {
    private final String name;
    private final String userData;
    private final FinishResult finishResult;
    private final String justOpenedList;
    private final String justSkippedList;
    private TaskOutputDTO taskOutput;

    public ProcessedTargetDTO(Target target) {
        this.name = target.getName();
        this.userData = target.getUserData() == null ? "No Data to show." : target.getUserData();
        this.finishResult = target.getFinishResult();
        justOpenedList = listToSting(target.getJustOpenedList());
        justSkippedList = listToSting(target.getSkippedList());
    }

    private String listToSting(List<Target> list) {
        if (list.size() == 0) {
            return "None";
        } else {
            StringBuilder str = new StringBuilder();
            list.forEach(target -> str.append(target.getName()).append(","));
            str.deleteCharAt(str.length() - 1);
            return str.toString();
        }
    }

    @Override
    public void setTaskOutput(TaskOutputDTO taskOutput) {
        this.taskOutput = taskOutput;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("START process target: ").append(name).append("\n");
        output.append(taskOutput.toString());
        output.append("Target Data:\n   ").append(userData).append("\n");
        output.append("FINISH process target: ").append(name).append("\n");
        if (finishResult.equals(FinishResult.WARNING)) {
            output.append("Target -").append(name).append("- finished with: SUCCESS WITH ").append(finishResult).append("\n");
        } else {
            output.append("Target -").append(name).append("- finished with: ").append(finishResult).append("\n");
        }
        output.append("Targets that just opened: ").append(justOpenedList).append("\n");
        output.append("Targets that had lost their chance to run: ").append(justSkippedList).append("\n");
        output.append("---------------------------------");

        return output.toString();
    }
}
