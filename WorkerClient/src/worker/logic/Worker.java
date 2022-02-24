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
    private int busyThreads;
    private ExecutorService threadsExecutor;
    private Map<String, WorkerExecution> workerExecutions; // list of registered tasks
    private final List<TaskTarget> targets;

    private TargetsRequestRefresher refresher;
    private boolean isAlive;

    //private List<ExecutionDTO> listedExecutions;
    // for task table in tasks screen:
    //              http req from engine: getExecutionByWorker

    public Worker() {
        credit = new SimpleIntegerProperty(0);
        busyThreads = 0;
        threadsCount = 0;
        targets = new ArrayList<>();
        workerExecutions = new HashMap<>();

        isAlive = true;

        threadsExecutor = Executors.newFixedThreadPool(threadsCount);

        new Thread(() -> run()).start();
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

    public int getFreeThreads() {
        return threadsCount - busyThreads;
    }

    public int getBusyThreads() {
        return busyThreads;
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void initThreadsExecutor(int threadsCount) {
        this.threadsCount = threadsCount;
        threadsExecutor = Executors.newFixedThreadPool(this.threadsCount);
        startRefresher();
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
        }
    }

    private void runTarget(TaskTarget target) {
        System.out.println(target.getName() + " / " + target.getExecutionName() + ": DONE");
        target.setStatus(TargetStatus.InProcess);
        /// implement task - compilation and simulation


        FinishedTargetDTO finishedTarget = new FinishedTargetDTO(target.getName(), target.getExecutionName(), target.getLogs(), this.name, FinishResultDTO.valueOf(target.getType().name()));

        String finishedTargetAsString = GSON_INST.toJson(finishedTarget);
        RequestBody body = RequestBody.create(finishedTargetAsString, MediaType.parse("application/json"));

        HttpClientUtil.runAsyncWithBody(Constants.SEND_TARGET, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    public void startRefresher() {
        refresher = new TargetsRequestRefresher(this, this::acceptTargets);
        new Timer().schedule(refresher, TARGET_REQ_REFRESH_RATE, TARGET_REQ_REFRESH_RATE);
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
        //Somethingbefore?
        threadsExecutor.shutdownNow();
    }
}
