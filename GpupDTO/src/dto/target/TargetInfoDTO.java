package dto.target;

import java.time.Duration;
import java.util.List;

public class TargetInfoDTO {
    private final List<String> dependsOnToOpenList;
    private final List<String> skippedBecauseList;
    private final List<String> serialSetsList;
    private final Duration waitingTimeInMs;
    private final Duration processingTimeInMs;
    private final String finishResult;
    private final String runResult;


    public TargetInfoDTO(List<String> dependsOnToOpenList, List<String> skippedBecauseList, List<String> serialSetsList, Duration waitingTimeInMs, Duration processingTimeInMs, String finishResult, String runResult) {
        this.dependsOnToOpenList = dependsOnToOpenList;
        this.skippedBecauseList = skippedBecauseList;
        this.serialSetsList = serialSetsList;
        this.waitingTimeInMs = waitingTimeInMs;
        this.processingTimeInMs = processingTimeInMs;
        this.finishResult = finishResult;
        this.runResult = runResult;
    }
}
