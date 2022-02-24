package old.component.task;

import old.component.target.FinishResult;
import old.component.target.oldTarget;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public interface Task {

    public FinishResult run(String targetName, String userData) throws InterruptedException;

    long getProcessingTime();

    void updateRelevantTargets(List<oldTarget> targets);

    int getParallelism();

    void incParallelism(Integer newVal);

    SimpleStringProperty getTaskOutput();
}
