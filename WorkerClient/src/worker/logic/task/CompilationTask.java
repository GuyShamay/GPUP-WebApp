package worker.logic.task;

import dto.execution.config.CompilationConfigDTO;
import worker.logic.target.TargetStatus;
import worker.logic.target.TaskTarget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

public class CompilationTask implements Task {
    private String srcDirectory;
    private String destDirectory;
    private String filePath;
    private Duration processingTime;

    public CompilationTask(CompilationConfigDTO configDTO) {
        this.srcDirectory = configDTO.getSrcDir();
        this.destDirectory = configDTO.getDestDir();
    }

    private void setPathFromFQN(String fqn) {
        filePath = srcDirectory + "\\" +
                fqn.replace(".", "\\") +
                ".java";
    }


    private StringBuilder getFullCommand(String[] command) {
        StringBuilder compilerFullCommand = new StringBuilder();
        for (String c : command) {
            compilerFullCommand.append(c).append(" ");
        }
        compilerFullCommand.append("\n");
        return compilerFullCommand;
    }


    public void run(TaskTarget target) throws InterruptedException {
        Instant start = Instant.now();
        target.setStartingTime(start.toString());
        StringBuilder log = new StringBuilder();
        log.append("File ").append(target.getName()).append(" is about to compile\n");
        setPathFromFQN(target.getUserData());
        int exitCode = -1;
        try {
            String[] command = {"javac", "-d", destDirectory, "-cp", srcDirectory, filePath};
            System.out.println(filePath);
            System.out.println(command);
            log.append(getFullCommand(command) + "\n");
            Process pb = new ProcessBuilder(command).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(pb.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                log.append(finalLine + "\n");
            }
            exitCode = pb.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Instant end = Instant.now();
        processingTime = Duration.between(start, end);
        target.setProcessingTime(String.valueOf(processingTime.toMillis()));
        if (exitCode == 0) {
            target.setStatus(TargetStatus.SUCCESS);
            log.append("File " + target.getName() + " Compiled in " + processingTime.toMillis() + "ms\n");
        } else {
            target.setStatus(TargetStatus.FAILURE);
            log.append("File " + target.getName() + " Failed to compile\n");
        }

        target.setLogs(log.toString());
    }
}
