package worker.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dto.execution.ExecutionDTO;
import dto.target.FinishResultDTO;
import dto.target.FinishedTargetDTO;
import dto.target.NewExecutionTargetDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.logic.target.TaskTarget;
import worker.logic.task.WorkerExecution;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static worker.client.util.Constants.GSON_INST;
import static worker.client.util.Constants.TASK_NAME;

public class Worker {
    private String name;
    private int threadsCount;
    private final IntegerProperty credit;
    private int busyThreads;
    private ExecutorService threadsExecutor;
    private Map<String, WorkerExecution> workerExecutions; // list of registered tasks
    private final List<TaskTarget> targets;
    BooleanProperty f;
    IntegerProperty i;

    //private List<ExecutionDTO> listedExecutions;
    // for task table in tasks screen:
    //              http req from engine: getExecutionByWorker

    public Worker() {
        credit = new SimpleIntegerProperty(0);
        busyThreads = 0;
        threadsCount = 0;
        targets = new ArrayList<>();
        workerExecutions = new HashMap<>();
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

    public void acceptTargets(List<TaskTarget> newTargets) {
        if (newTargets != null || !newTargets.isEmpty()) {
            targets.addAll(newTargets);
        }

    }

    public void run() {
        List<Future<?>> futures = new ArrayList<>();

        while (true) {
            workerExecutions.forEach((s, exec) -> {
                TaskTarget target = targets.stream()
                        .filter(taskTarget -> taskTarget.getStatus() == null && taskTarget.getExecutionName().equals(exec.getName()))
                        .findFirst().get(); // the first target from the task that didn't process yet
                Runnable r = () -> {
                    try {
                        runTarget(target);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                Future<?> f = threadsExecutor.submit(r);
                futures.add(f);
            });
        }
    }

    private void runTarget(TaskTarget target) {
        System.out.println(target.getName() + " / " + target.getExecutionName() + ": DONE");
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
}
