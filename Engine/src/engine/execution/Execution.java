package engine.execution;

import dto.execution.config.ConfigDTO;
import dto.execution.config.ExecutionConfigDTO;
import dto.target.FinishedTargetDTO;
import dto.target.NewExecutionTargetDTO;
import dto.target.TargetDTO;
import engine.graph.TargetGraph;
import engine.graph.TargetGraphUtil;
import engine.progressdata.ProgressData;
import engine.target.FinishResult;
import engine.target.Result;
import engine.target.RunResult;
import engine.target.Target;


import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Execution {
    private TargetGraph taskGraph;
    private String name;
    private String creatingUser;
    private int currentWorkersCount;
    private int price;
    private int progress;
    private ExecutionStatus status;
    private ExecutionType type;
    private final List<String> workers;
    private ConfigDTO executionDetails;
    private ProgressData progressData;
    private List<String> logs;
    public final static String WORKING_DIR = "C:\\gpup-working-dir\\";

    // Run Execution Managing Fields: (Not pass by DTO)
    private boolean isStarted; // PLAY -> to true
    private List<Target> waitingList;
    private Set<Target> doneTargets;
    private boolean isEnded;

    public Execution() {
        status = ExecutionStatus.New;
        currentWorkersCount = 0;
        progress = 0;
        progressData = new ProgressData();
        workers = new ArrayList<>();

        doneTargets = new HashSet<>();
        logs = new ArrayList<>();
    }


    // -----------------------------------------------------------------------------

    public ConfigDTO getExecutionDetails() {
        return executionDetails;
    }

    public void setExecutionDetails(ConfigDTO executionDetails) {
        this.executionDetails = executionDetails;
    }

    public double getProgress() {
        return (double) progress / taskGraph.getCount();
    }

    public ExecutionType getType() {
        return type;
    }

    public void setType(ExecutionType type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public TargetGraph getTaskGraph() {
        return taskGraph;
    }

    public void setTaskGraph(TargetGraph taskGraph) {
        this.taskGraph = taskGraph;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatingUser() {
        return creatingUser;
    }

    public void setCreatingUser(String creatingUser) {
        this.creatingUser = creatingUser;
    }

    public int getCurrentWorkersCount() {
        return currentWorkersCount;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public List<String> getWorkers() {
        return workers;
    }

    //----------------------------------------------------------------------------------

    public void createTaskGraph(ExecutionConfigDTO execConfig, TargetGraph baseGraph) {
        this.taskGraph = baseGraph.clone(execConfig);
    }

    public void createTaskGraph(TargetGraph baseGraph) {
        this.taskGraph = baseGraph.clone();
    }

    public void fromScratchReset() {
        this.taskGraph.fromScratchReset();
    }

    public synchronized void addWorker(String name) {
        workers.add(name);
        currentWorkersCount++;
    }

    public synchronized void removeWorker(String name) {
        workers.remove(name);
        currentWorkersCount--;
    }

    public boolean isWorkerExist(String name) {
        return workers.contains(name);
    }

    public synchronized void incrementProgress() {
        if (progress < taskGraph.getCount()) {
            progress++;
            System.out.println(progress);
            if (progress == taskGraph.getCount()) {
                isEnded = true;
            }
        }
    }

    public boolean checkValidIncremental() {
        // if incremental chosen, and all targets are finished -> from scratch
        return taskGraph.checkValidIncremental();
    }

    public ProgressData getProgressData() {
        return progressData;
    }

    public void updateLeavesAndIndependentsToWaiting() {
        taskGraph.updateLeavesAndIndependentToWaiting(progressData); // NTU: target.getNameAndWorker()
        // DEBUG: progressData might not be updated...
    }

    public boolean isAllowedToPlay() {
        return status == ExecutionStatus.Paused | status == ExecutionStatus.New;
    }

    private void initialize() {
        if (!taskGraph.hasCircuit()) {
            status = ExecutionStatus.Active;
            taskGraph.getTargetsListByName().forEach(targetName -> progressData.initToFrozen(targetName));
            taskGraph.buildTransposeGraph();
            taskGraph.clearJustOpenAndSkippedLists();
            updateLeavesAndIndependentsToWaiting();
            waitingList = taskGraph.getWaitingTargets();
            return;
        }
        throw new RuntimeException("The task's graph has a circuit, can't run execution");
    }

    public void stop() {
        status = ExecutionStatus.Stopped;
    }

    public void pause() {
        status = ExecutionStatus.Paused;
    }

    public void resume() {
        status = ExecutionStatus.Active;
    }

    public void play() {
        initialize(); // circuit ? throw runTimeException -> catch in servlet

        while (status.equals(ExecutionStatus.Active) || status.equals(ExecutionStatus.Paused)) {
            if (isAllDone()) {
                status = ExecutionStatus.Done;
            }
        }
//        Thread.currentThread().interrupt();
    }

    private boolean isAllDone() {
        return taskGraph.getTargetMap().values().stream().filter(target -> !target.getRunResult().equals(RunResult.SKIPPED) && !(target.getRunResult() == RunResult.FINISHED)).count() == 0;
    }

    public synchronized List<NewExecutionTargetDTO> requestNewTargets(int threadsCount) {

        int targetsPerWorker = waitingList.size() / workers.size();
        if (targetsPerWorker == 0) { // waiting is empty OR more workers than waiting
            targetsPerWorker = waitingList.size() == 0 ? 0 : 1;
        }
        int totalToSend = Math.min(targetsPerWorker, threadsCount);

        if (totalToSend != 0) {
            List<NewExecutionTargetDTO> list = new ArrayList<>();
            for (int i = 0; i < totalToSend; i++) {
                Target currentTarget = waitingList.remove(0);
                changeRunResult(RunResult.WAITING, RunResult.INPROCESS, currentTarget);
                NewExecutionTargetDTO t = new NewExecutionTargetDTO(currentTarget, this.name);
                list.add(t);
            }
            return list;
        }
        return null;
    }

    private void changeRunResult(Result from, Result to, Target target) {
        if (from != to) {
            if (to instanceof RunResult)
                target.setRunResult((RunResult) to);
            else
                target.setFinishResult((FinishResult) to);
            progressData.move(from, to, target.getName());
        }
    }

    public void setFinishedTarget(FinishedTargetDTO finishedTarget) {
        Target target = taskGraph.getTargetMap().get(finishedTarget.getName());
        target.setFinishResult(FinishResult.valueOf(finishedTarget.getFinishResult().toString()));
        updateProgressBar(target);
        changeRunResult(RunResult.INPROCESS, target.getFinishResult(), target);
        target.setStartRunningTime(Instant.parse(finishedTarget.getStartingTime()));
        target.setTaskRunDuration(Duration.between(target.getStartRunningTime(), Instant.now()));

        try {
            writeTLogsToFile(finishedTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }

        addFinishedTargetLog(finishedTarget.getLogs());
        updateProgressBar(target);

        updateGraphAfterTaskResult(waitingList, target);
    }

    private void writeTLogsToFile(FinishedTargetDTO finishedTarget) throws IOException {
        createTaskDirectoryIfNeeded();
        String path = WORKING_DIR + this.name;
        String fileName = finishedTarget.getName() + ".log";
        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path + "\\" + fileName)))) {

            // write to file:
            out.write(finishedTarget.getLogs());
        }
    }


    public void createTaskDirectoryIfNeeded() {
        String path = WORKING_DIR + this.name;

        File taskDirectory = new File(path);
        if (!taskDirectory.exists()) {
            if (!taskDirectory.mkdir()) {
                throw new RuntimeException("Failure with creating the Task's Directory");
            }
        }
    }

    private synchronized void addFinishedTargetLog(String log) {
        this.logs.add(log);

    }

    private synchronized void updateGraphAfterTaskResult(List<Target> waitingList, Target currentTarget) {
        currentTarget.setRunResult(RunResult.FINISHED);
        if (currentTarget.getFinishResult().equals(FinishResult.FAILURE)) {
            taskGraph.dfsTravelToUpdateSkippedList(currentTarget);
            addSkippedToDoneSet(currentTarget);
            changeRunResultOfList(currentTarget.getSkippedList(), RunResult.FROZEN, RunResult.SKIPPED);
            taskGraph.updateTargetAdjAfterFinishWithFailure(currentTarget);
        } else {
            taskGraph.updateTargetAdjAfterFinishWithoutFailure(progressData, waitingList, currentTarget);
        }
    }

    private synchronized void changeRunResultOfList(List<Target> skippedList, RunResult from, RunResult to) {
        for (Target t : skippedList) {
            changeRunResult(from, to, t);
        }
    }

    private void addSkippedToDoneSet(Target currentTarget) {
        currentTarget.getSkippedList().forEach(target -> {
            updateProgressBar(target);
        });
    }

    private synchronized void updateProgressBar(Target target) {
        int sizeBefore = doneTargets.size();
        doneTargets.add(target);
        if (sizeBefore < doneTargets.size())
            incrementProgress();
    }

    public int getLogsVersions() {
        return logs.size();
    }

    public synchronized List<String> getLogsEntries(int version) {
        if (version < 0 || version > logs.size()) {
            version = 0;
        }
        return logs.subList(version, logs.size());
    }

    public ExecutionStatus getExecutionStatus() {
        return status;

    }

    public TargetDTO getTargetDTORealTime(String targetName) {
        Target t = taskGraph.getTargetMap().get(targetName);
        TargetDTO res = new TargetDTO(t);
        res.initRunningFields(t);
        return res;
    }

    public void createTaskGraphIncremental(TargetGraph baseTaskGraph) {
        List<String> targets = new ArrayList<>();
        for (Target t : baseTaskGraph.getTargetMap().values()) {
            if (t.getFinishResult() == FinishResult.FAILURE || t.getFinishResult() == null) {
                targets.add(t.getName());
            }
        }

        this.taskGraph = new TargetGraph(baseTaskGraph.getName(), baseTaskGraph.getCreatingUsername());
        Map<String, List<Target>> dependsOn = new HashMap<>();
        Map<String, Target> targetMap = new HashMap<>();

        targets.forEach(targetName -> {
            dependsOn.put(targetName, new LinkedList<>());
            try {
                targetMap.put(targetName, baseTaskGraph.getTargetMap().get(targetName).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });

        taskGraph.setDependsOnList(dependsOn);
        taskGraph.setTargetMap(targetMap);

        targetMap.values().forEach(target -> {
            target.getDependsOnList().forEach(adj -> {
                if (targets.contains(adj.getName())) taskGraph.addEdge(target.getName(), adj);
            });
        });
    }

    public void prepareGraphIncremental() {
        fromScratchReset();
        TargetGraphUtil.updateTargetsType(taskGraph.getTargetMap());
        updateLeavesAndIndependentsToWaiting();
    }
}
