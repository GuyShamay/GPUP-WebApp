package old.dto;
import old.component.target.oldTarget;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDTO implements GPUPConsumer {

    List<TargetDTO> targets;
    private final String name;
    private int successTargets;
    private int warningsTargets;
    private int failureTargets;
    private int skippedTargets;

    private Duration duration;
    private String taskStatus;


    public StatisticsDTO(String taskName) {
        name = taskName;
        successTargets = 0;
        warningsTargets = 0;
        failureTargets = 0;
        skippedTargets = 0;
    }

    public String getName() {
        return name;
    }

    public int getSuccessTargets() {
        return successTargets;
    }

    public int getWarningsTargets() {
        return warningsTargets;
    }

    public int getFailureTargets() {
        return failureTargets;
    }

    public int getSkippedTargets() {
        return skippedTargets;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    // will be called by the engine when task is done
    public void updateTaskResults(List<oldTarget> targets, Duration totalDuration) {
        this.targets = new ArrayList<>();
        targets.forEach(((target) -> {
            this.targets.add(new TargetDTO(target));
        }));
        this.duration = totalDuration;
        calsRunResults();
    }

    private void calsRunResults() {

        targets.forEach((target -> {
            if (target.getFinishResult() != null) {
                switch (target.getFinishResult()) {
                    case SUCCESS:
                        successTargets++;
                        break;
                    case WARNING:
                        warningsTargets++;
                        break;
                    case FAILURE:
                        failureTargets++;
                        break;
                }
            } else
                skippedTargets++;
        }));

        if(skippedTargets!= 0)
        {
            taskStatus = "Task Failed";
        }else{
            taskStatus = "Task Succeeded";
        }
    }

    @Override
    public String toString() {

        String res = "~~~~~~~~~~~~~~ RUN SUMMARY ~~~~~~~~~~~~~~" +
                "\nTotal Task Run Duration : " + String.format("%d:%02d:%02d",
                duration.toHours(),
                duration.toMinutes(),
                duration.getSeconds()) +
                "\n                STATISTICS                  " +
                "\n SUCCES TARGETS........." + successTargets +
                "\n WARNING TARGETS........" + warningsTargets +
                "\n FAILURE TARGETS........" + failureTargets +
                "\n SKIPPED TARGETS........" + skippedTargets +
                "\n\n                 TARGETS                   ";

        for (TargetDTO t : targets) {
            res += t.toString();
        }
        return res;
    }
}
