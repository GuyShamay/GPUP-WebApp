package engine.execution;

import dto.execution.config.ConfigDTO;
import dto.execution.config.ExecutionConfigDTO;
import dto.target.NewExecutionTargetDTO;
import engine.graph.TargetGraph;
import engine.progressdata.ProgressData;
import engine.target.FinishResult;
import engine.target.Result;
import engine.target.RunResult;
import engine.target.Target;
import old.component.target.TargetType;

import java.util.ArrayList;
import java.util.List;

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

    // Run Execution Managing Fields: (Not pass by DTO)
    private boolean pause; // RESUME -> to false, PAUSE -> to true
    private boolean play; // STOP -> to false, PLAY -> to true
    private boolean isStarted; // PLAY -> to true
    private List<Target> waitingList;

    public Execution() {
        status = ExecutionStatus.New;
        currentWorkersCount = 0;
        progress = 0;
        progressData = new ProgressData();
        workers = new ArrayList<>();
        play = false;
        pause = false;
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
        }
    }

    public void checkValidIncremental() {
        // if incremental chosen, and all targets are finished -> from scratch
        taskGraph.checkValidIncremental();
    }

    public ProgressData getProgressData() {
        return progressData;
    }

    public void updateLeavesAndIndependentsToWaiting() {
        taskGraph.updateLeavesAndIndependentToWaiting(progressData); // NTU: target.getNameAndWorker()
        // DEBUG: progressData might not be updated...
    }

    public boolean isAllowedToPlay() {
        return !isStarted;
    }

    private void initialize() {
        if (!taskGraph.hasCircuit()) {
            play = true;
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
        play = false;
        status = ExecutionStatus.Stopped;
        System.out.println("stopped");

    }

    public void pause() {
        pause = true;
        status = ExecutionStatus.Paused;
        System.out.println("paused");
    }

    public void resume() {
        pause = false;
        status = ExecutionStatus.Active;
        System.out.println("resumed");
    }

    public void play() {
        initialize(); // circuit ? throw runTimeException -> catch in servlet
        isStarted = true;

        System.out.println("on");

        while (play) {
            while (pause) {
                // update message: PAUSED
            }
        }
        System.out.println("done");
        status = ExecutionStatus.Done;
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
                NewExecutionTargetDTO t = new NewExecutionTargetDTO(currentTarget);
                t.setExecuitonName(this.name);
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
}
