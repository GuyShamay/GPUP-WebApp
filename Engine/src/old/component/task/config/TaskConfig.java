package old.component.task.config;

import engine.target.TargetsRelationType;
import old.component.task.ProcessingType;
import engine.execution.ExecutionType;

import java.util.List;

public class TaskConfig {
    private boolean isAllTargets;
    private ProcessingType processingType;
    private List<String> customTargets;
    private String whatIfTarget;
    private TargetsRelationType whatIfRelation;
    private int threadsParallelism;
    private Config specificConfig;
    private ExecutionType taskType;

    public boolean isAllTargets() {
        return isAllTargets;
    }

    public void setAllTargets(boolean allTargets) {
        isAllTargets = allTargets;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public void setProcessingType(ProcessingType processingType) {
        this.processingType = processingType;
    }

    public List<String> getCustomTargets() {
        return customTargets;
    }

    public void setCustomTargets(List<String> customTargets) {
        this.customTargets = customTargets;
    }

    public String getWhatIfTarget() {
        return whatIfTarget;
    }

    public void setWhatIfTarget(String whatIfTarget) {
        this.whatIfTarget = whatIfTarget;
    }

    public TargetsRelationType getWhatIfRelation() {
        return whatIfRelation;
    }

    public void setWhatIfRelation(TargetsRelationType whatIfRelation) {
        this.whatIfRelation = whatIfRelation;
    }

    public int getThreadsParallelism() {
        return threadsParallelism;
    }

    public void setThreadsParallelism(int threadsParallelism) {
        this.threadsParallelism = threadsParallelism;
    }

    public Config getConfig() {
        return specificConfig;
    }

    public void setConfig(Config config) {
        this.specificConfig = config;
        taskType = specificConfig instanceof CompileConfig ? ExecutionType.Compilation : ExecutionType.Simulation;
    }

    public ExecutionType getTaskType() { return taskType; }
}
