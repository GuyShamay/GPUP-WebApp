package old.component.task.compile;

import old.component.target.FinishResult;
import old.component.target.Target;
import old.component.task.Task;
import old.component.task.config.CompileConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class CompileTask implements Task {
    private String srcDirectory;
    private String destDirectory;
    private String filePath;
    private int parallelism;
    private Duration processingTime;
    private final SimpleStringProperty taskOutput;

    public CompileTask(CompileConfig config, int parallelism) {
        this.srcDirectory = config.getSrcDir();
        this.destDirectory = config.getDestDir();
        this.parallelism = parallelism;
        taskOutput = new SimpleStringProperty();
    }

    private void setPathFromFQN(String fqn) {
        filePath = srcDirectory + "\\" +
                fqn.replace(".", "\\") +
                ".java";
    }

    @Override
    public FinishResult run(String targetName, String userData) throws InterruptedException {
        Instant start = Instant.now();
        Platform.runLater(() -> taskOutput.setValue("File " + targetName + " is about to compile\n"));
        setPathFromFQN(userData);
        FinishResult result;
        int exitCode = -1;
        try {
            String[] command = {"javac", "-d", destDirectory, "-cp", srcDirectory, filePath};

            Platform.runLater(() -> taskOutput.setValue(getFullCommand(command).toString()));
            Process pb = new ProcessBuilder(command).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(pb.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                Platform.runLater(() -> taskOutput.setValue(finalLine));
            }
            exitCode = pb.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Instant end = Instant.now();
        processingTime = Duration.between(start, end);
        if (exitCode == 0) {
            result = FinishResult.SUCCESS;
            Platform.runLater(() -> taskOutput.setValue("File " + targetName + " Compiled in " + processingTime.toMillis() + "ms\n"));
        } else {
            result = FinishResult.FAILURE;
            Platform.runLater(() -> taskOutput.setValue("File " + targetName + " Failed to compile\n"));
        }
        return result;
    }

    private StringBuilder getFullCommand(String[] command) {
        StringBuilder compilerFullCommand = new StringBuilder();
        for (String c : command) {
            compilerFullCommand.append(c).append(" ");
        }
        compilerFullCommand.append("\n");
        return compilerFullCommand;
    }

    @Override
    public long getProcessingTime() {
        return processingTime.toMillis();
    }

    @Override
    public void updateRelevantTargets(List<Target> targets) {

    }

    @Override
    public int getParallelism() {
        return parallelism;
    }

    @Override
    public void incParallelism(Integer newVal) {
        parallelism = newVal;
    }

    @Override
    public SimpleStringProperty getTaskOutput() {
        return taskOutput;

    }

}
