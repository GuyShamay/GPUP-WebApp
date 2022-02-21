package engine.target;


public enum RunResult implements Result {
    FROZEN,
    SKIPPED,
    WAITING,
    INPROCESS,
    FINISHED
}
