package worker.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dto.execution.ExecutionDTO;
import dto.target.FinishResultDTO;
import dto.target.FinishedTargetDTO;
import dto.target.NewExecutionTargetDTO;
import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.logic.target.TargetStatus;
import worker.logic.target.TaskTarget;
import worker.logic.task.TargetsRequestRefresher;
import worker.logic.task.WorkerExecution;
import worker.logic.task.WorkerExecutionStatus;

import java.io.IOException;
import java.security.Key;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static worker.client.util.Constants.*;
import static worker.client.util.Constants.TARGET_REQ_REFRESH_RATE;

public class Worker {
    private String name;
    private int threadsCount;
    private final IntegerProperty credit;
    private final IntegerProperty busyThreads;
    private ExecutorService threadsExecutor;
    private Map<String, WorkerExecution> workerExecutions; // list of registered tasks
    private final List<TaskTarget> targets;
    private TargetsRequestRefresher refresher;
    private Timer timer;
    private boolean isAlive;

    //private List<ExecutionDTO> listedExecutions;
    // for task table in tasks screen:
    //              http req from engine: getExecutionByWorker
    public Worker() {
        credit = new SimpleIntegerProperty(0);
        busyThreads = new SimpleIntegerProperty(0);
        threadsCount = 0;
        targets = new ArrayList<>();
        workerExecutions = new HashMap<>();
        isAlive = true;
    }

    public IntegerProperty busyThreadsProperty() {
        return busyThreads;
    }

    public ObservableList<TaskTarget> getTargets() {
        return FXCollections.observableArrayList(targets);
    }

    public int getCredit() {
        return credit.get();
    }

    public IntegerProperty creditProperty() {
        return credit;
    }

    public void addCredit(int credit) {
        this.credit.add(credit);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBusyThreads() {
        return busyThreads.get();
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void initThreadsExecutor(int threadsCount) {
        this.threadsCount = threadsCount;
        threadsExecutor = Executors.newFixedThreadPool(this.threadsCount);
        startRefresher();
        new Thread(() -> run()).start();
    }

    public boolean isRegisterAny() {
        return !workerExecutions.isEmpty();
    }

    public synchronized void addExecution(WorkerExecution workerExecution) {
        workerExecutions.put(workerExecution.getName(), workerExecution);
    }


    public List<String> getTasksListByName() {
        return new ArrayList<>(workerExecutions.keySet());
    }

    public void run() {
        List<Future<?>> futures = new ArrayList<>();

        while (isAlive) {
            workerExecutions.forEach((s, exec) -> {
                targets.forEach(taskTarget -> {
                    if (taskTarget.getStatus() == null && taskTarget.getExecutionName().equals(exec.getName())) {
                        taskTarget.setStatus(TargetStatus.InProcess);
                        Runnable r = () -> {
                            try {
                                runTarget(taskTarget);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                        Future<?> f = threadsExecutor.submit(r);
                        futures.add(f);
                    }
                });
            });
//
//                TaskTarget target = targets.stream()
//                        .filter(taskTarget -> taskTarget.getStatus() == null && taskTarget.getExecutionName().equals(exec.getName()))
//                        .findFirst().get(); // the first target from the task that didn't process yet
//                if(target!=null) {
//                    Runnable r = () -> {
//                        try {
//                            runTarget(target);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    };
//                    Future<?> f = threadsExecutor.submit(r);
//                    futures.add(f);
//                }
//            });
        }
    }

    private void runTarget(TaskTarget target) {
        System.out.println(target.getName() + " / " + target.getExecutionName() + ": DONE");
        /// implement task - compilation and simulation


//        FinishedTargetDTO finishedTarget = new FinishedTargetDTO(target.getName(), target.getExecutionName(), target.getLogs(), this.name, FinishResultDTO.valueOf(target.getType().name()));
//
//        String finishedTargetAsString = GSON_INST.toJson(finishedTarget);
//        RequestBody body = RequestBody.create(finishedTargetAsString, MediaType.parse("application/json"));
//
//        HttpClientUtil.runAsyncWithBody(Constants.SEND_TARGET, body, new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//
//            }
//        });
    }

    public void startRefresher() {
        refresher = new TargetsRequestRefresher(this, this::acceptTargets);
        timer = new Timer();
        timer.schedule(refresher, TARGET_REQ_REFRESH_RATE, TARGET_REQ_REFRESH_RATE);
    }

    private void acceptTargets(List<NewExecutionTargetDTO> newTargets) {
        newTargets.forEach(t -> {
            TaskTarget target = new TaskTarget(t);
            WorkerExecution workerExecution = workerExecutions.get(t.getExecutionName());
            target.setPayedPrice(workerExecution.getPrice());
            target.setType(workerExecution.getType());
            target.setStatus(null);
            // maybe add configDTO
            targets.add(target);
        });
    }

    public boolean isAvailableThreads() {
        return (threadsCount - ((ThreadPoolExecutor) threadsExecutor).getActiveCount() > 0);
    }

    public Set<String> getWorkerExecutions() {
        return workerExecutions.keySet();
    }

    public void shutdown() {
        isAlive = false;
        if (refresher != null && timer != null) {
            refresher.cancel();
            timer.cancel();
        }
        threadsExecutor.shutdownNow();
    }

    public WorkerExecutionStatus getWorkerExecutionStatus(String taskName) {
        return workerExecutions.get(taskName).getExecutionStatus();
    }

    public void pauseWorkerExecution(String taskName) {
        workerExecutions.get(taskName).pause();
    }

    public void resumeWorkerExecution(String taskName) {
        workerExecutions.get(taskName).activate();
    }

    public void unregisterWorkerExecution(String taskName) {
        workerExecutions.get(taskName).stop();
        // remove from map
        // send http req to server to unregiter
    }

    public ObservableList<WorkerExecution> getObservableWorkerExecutions() {
        return FXCollections.observableArrayList(workerExecutions.values());
    }


}
