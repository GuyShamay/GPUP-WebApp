package worker.logic.task;

import dto.execution.config.SimulationConfigDTO;
import dto.target.FinishResultDTO;
import worker.logic.target.TargetStatus;
import worker.logic.target.TaskTarget;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class SimulationTask implements Task {

    private final int processingTimeInMs;
    private final Boolean isRandomProcTime;
    private final float successProb;
    private final float successWithWarningsProb;
    private long sleepingTime;
    private final Random random;

    public SimulationTask(SimulationConfigDTO details) {
        processingTimeInMs = details.getProcessingTime();
        isRandomProcTime = details.getIsRandom();
        successProb = details.getSuccessProb();
        successWithWarningsProb = details.getSuccessWithWarningsProb();
        random = new Random();
    }

    public void run(TaskTarget target) throws InterruptedException {

        Instant start, end;
        start = Instant.now();
        target.setStartingTime(start.toString());
        float LuckyNumber = (float) Math.random();
        String runningLogs = "";
        calcSingleTargetProcessingTimeInMs();
        runningLogs += target.getName() + ": Sleeping Time - " + sleepingTime + " ms\n";

        TargetStatus res = LuckyNumber < successProb ? TargetStatus.SUCCESS : TargetStatus.FAILURE;

        if (res == TargetStatus.SUCCESS) {
            LuckyNumber = (float) Math.random();
            res = LuckyNumber < successWithWarningsProb ? TargetStatus.WARNING : TargetStatus.SUCCESS;
        }
        runningLogs += target.getName() + " going to sleep...\n";
        Thread.sleep(sleepingTime);
        end = Instant.now();

        runningLogs += target.getName() + " woke up!\n";
        target.setProcessingTime(String.valueOf(Duration.between(start, end).toMillis()));
        runningLogs += "Actual Processing time: " + target.getProcessingTime() + " ms\n";

        target.setLogs(runningLogs);
        target.setStatus(res);
    }

    private void calcSingleTargetProcessingTimeInMs() {
        int res = 0;
        if (isRandomProcTime) {
            res = random.nextInt(processingTimeInMs);
        } else {
            res = processingTimeInMs;
        }
        sleepingTime = res;
    }
}
