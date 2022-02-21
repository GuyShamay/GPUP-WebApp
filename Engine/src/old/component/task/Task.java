package old.component.task;

import old.component.target.FinishResult;
import old.component.target.Target;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public interface Task {

    public FinishResult run(String targetName, String userData) throws InterruptedException;

    long getProcessingTime();

    void updateRelevantTargets(List<Target> targets);

    int getParallelism();

    void incParallelism(Integer newVal);

    SimpleStringProperty getTaskOutput();
}
