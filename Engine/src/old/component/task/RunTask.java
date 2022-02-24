package old.component.task;

import engine.progressdata.ProgressData;
import old.component.target.FinishResult;
import engine.target.Result;
import engine.target.RunResult;
import old.component.target.Target;
import old.component.targetgraph.TargetGraph;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class RunTask extends Task<Boolean> {

    private TargetGraph subTargetGraph;
    private ExecutorService threadExecutor;
    private old.component.task.Task task;
    private boolean runPaused;
    private final Object condition;
    private final Object changeRunResult;
    private boolean doneRunning;
    private ProgressData progressData;
    // private List<String> currRunTargets;
    private Set<Target> doneTargets;

    public RunTask(old.component.task.Task task) {
        this.task = task;
        runPaused = false;
        doneRunning = false;
        progressData = new ProgressData();
        condition = new ReentrantLock();
        changeRunResult = new ReentrantLock();
        doneTargets = new HashSet<>();
    }

    public SimpleStringProperty taskOutputProperty() {
        return task.getTaskOutput();
    }

    public void setSubTargetGraph(TargetGraph subTargetGraph) {
        this.subTargetGraph = subTargetGraph;
    }

    public void initProgressData(ProcessingType processingType) {
        subTargetGraph.getTargetsMap().forEach((s, target) -> progressData.initToFrozen(s));
    }

    @Override
    protected Boolean call() {
        updateMessage("Task Starting");
        updateProgress(0, subTargetGraph.count());
        runTask();
        updateMessage("Task Finished");
        return true;
    }

    private void runTask() {
        List<Target> waitingList = subTargetGraph.getAllWaitingTargets();
        List<Future<?>> futures = new ArrayList<>();
        threadExecutor = Executors.newFixedThreadPool(task.getParallelism());

        do {
            while (!waitingList.isEmpty()) {
                while (runPaused) {
                    updateMessage("Task Paused");
                    handlePause(futures, waitingList);
                    //Thread.sleep(1000);
                }
                Target currentTarget = waitingList.remove(0);
                while (currentTarget.isLock()) {
                    Thread.yield();
                }
                if (!currentTarget.isLock()) {
                    Runnable r = () -> {
                        try {
                            runTarget(waitingList, currentTarget);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    };
                    Future<?> f = threadExecutor.submit(r);
                    futures.add(f);

                }
            }
        } while (!AllThreadsDone(futures));


        doneRunning = true;
        threadExecutor.shutdownNow();
    }

    public void resume() {
        if (!doneRunning) {
            runPaused = false;
            synchronized (condition) {
                condition.notify();
            }
        }
    }

    public void pause() {
        if (!doneRunning)
            runPaused = true;
    }

    public boolean isRunPaused() {
        return runPaused;
    }

    private void handlePause(List<Future<?>> futures, List<Target> waitingList) {
        cancelAllFutures(futures);
        try {
            synchronized (condition) {
                condition.wait();
                ((ThreadPoolExecutor) threadExecutor).setCorePoolSize(task.getParallelism());
                updateWaitingList(waitingList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWaitingList(List<Target> waitingList) {
        subTargetGraph.getTargetsMap().forEach(((s, target) -> {
            if (target.getRunResult().equals(RunResult.WAITING) && !waitingList.contains(target)) {
                waitingList.add(target);
            }
        }));
    }

    private void cancelAllFutures(List<Future<?>> futures) {
        futures.forEach(future -> future.cancel(false));
    }

    private boolean AllThreadsDone(List<Future<?>> futures) {
        boolean allDone = true;
        for (Future<?> future : futures) {
            allDone &= future.isDone(); // check if future is done
        }
        return allDone;
    }

    private void runTarget(List<Target> waitingList, Target currentTarget) throws InterruptedException {
       /* subTargetGraph.lockSerialSetOf(currentTarget);

        currentTarget.setStartRunningTime();
        updateMessage("Target " + currentTarget.getName() + " Starting");
        changeRunResult(RunResult.WAITING, RunResult.INPROCESS, currentTarget);
        currentTarget.setFinishResult(task.run(currentTarget.getName(), currentTarget.getUserData()));
        updateMessage("Target " + currentTarget.getName() + " Finished");

        synchronized (changeRunResult) {
            updateProgressBar(currentTarget);
        }
        changeRunResult(RunResult.INPROCESS, currentTarget.getFinishResult(), currentTarget);
        updateGraphAfterTaskResult(waitingList, currentTarget);
        subTargetGraph.unlockSerialSetOf(currentTarget);*/
    }

    private void changeRunResult(Result from, Result to, Target target) {
       /* synchronized (changeRunResult) {
            if (from != to) {
                if (to instanceof RunResult)
                    target.setRunResult((RunResult) to);
                else
                    target.setFinishResult((FinishResult) to);
                Platform.runLater(() -> progressData.move(from, to, target.getName()));
            }
        }*/
    }


    private void updateGraphAfterTaskResult(List<Target> waitingList, Target currentTarget) {
       /* synchronized (changeRunResult) {
            currentTarget.setRunResult(RunResult.FINISHED);
            if (currentTarget.getFinishResult().equals(FinishResult.FAILURE)) {
                subTargetGraph.dfsTravelToUpdateSkippedList(currentTarget);
                addSkippedToDoneSet(currentTarget);
                changeRunResultOfList(currentTarget.getSkippedList(), RunResult.FROZEN, RunResult.SKIPPED);
                subTargetGraph.updateTargetAdjAfterFinishWithFailure(currentTarget);
            } else {
                subTargetGraph.updateTargetAdjAfterFinishWithoutFailure(progressData, waitingList, currentTarget);
            }
        }*/
    }

    private void updateProgressBar(Target target) {
        doneTargets.add(target);
        updateProgress(doneTargets.size(), subTargetGraph.count());
    }

    private void addSkippedToDoneSet(Target currentTarget) {
        currentTarget.getSkippedList().forEach(target -> {
            updateProgressBar(target);
        });
    }

    private void changeRunResultOfList(List<Target> list, Result from, Result to) {
        synchronized (changeRunResult) {
            for (Target t : list) {
                changeRunResult(from, to, t);
            }
        }
    }

    public ProgressData getProgressData() {
        return progressData;
    }

}
