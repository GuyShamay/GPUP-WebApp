package worker.logic.task;

public enum WorkerExecutionStatus {
    Active,
    Paused,
    StoppedByAdmin,
    Finished,
    Unregistered,
}
