package worker.logic;

import dto.execution.LightWorkerExecution;
import dto.target.FinishResultDTO;
import dto.target.FinishedTargetDTO;
import dto.target.NewExecutionTargetDTO;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.logic.target.TargetStatus;
import worker.logic.target.TaskTarget;
import worker.logic.task.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static worker.client.util.Constants.*;
import static worker.client.util.Constants.TARGET_REQ_REFRESH_RATE;

public class Worker {
    private String name;
    private boolean isAlive;
    private int threadsCount;
    private final IntegerProperty credit;
    private final IntegerProperty busyThreads;
    private ExecutorService threadsExecutor;
    private List<Future<?>> futures = new ArrayList<>();
    private Map<String, WorkerExecution> workerExecutions; // list of registered tasks
    private final List<TaskTarget> targets;
    private Map<WorkerExecution, List<TaskTarget>> targetsPerExec;
    private Thread threadsManager;

    private Timer targetsRequestTimer;
    private Timer lightWorkerExecTimer;
    private TargetsRequestRefresher targetsRequestRefresher;
    private LightWorkerExecutionRefresher lightWorkerExecutionRefresher;


    public Worker() {
        credit = new SimpleIntegerProperty(0);
        busyThreads = new SimpleIntegerProperty(0);
        threadsCount = 0;
        targets = new ArrayList<>();
        workerExecutions = new HashMap<>();
        targetsPerExec = new HashMap<>();
        isAlive = true;
        threadsManager = new Thread(() -> executorManager());
    }

    private void executorManager() {
        while (isAlive) {
            int active = ((ThreadPoolExecutor) threadsExecutor).getActiveCount();
            if (busyThreads.get() != active)
                Platform.runLater(() -> busyThreads.set(active));
        }
        System.out.println("about to die");
    }

    public IntegerProperty busyThreadsProperty() {
        return busyThreads;
    }

    public ObservableList<TaskTarget> getObservableTargets() {
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

    public int getThreadsCount() {
        return threadsCount;
    }

    public void initThreadsExecutor(int threadsCount) {
        this.threadsCount = threadsCount;
        threadsExecutor = Executors.newFixedThreadPool(this.threadsCount);
        threadsManager.start();
        startRequestRefresher();
        startLightRefresher();

    }

    public boolean isRegisterAny() {
        return !workerExecutions.isEmpty();
    }

    public synchronized void addExecution(WorkerExecution workerExecution) {
        workerExecutions.put(workerExecution.getName(), workerExecution);
        targetsPerExec.put(workerExecution, new ArrayList<>());
    }


    public List<String> getTasksListByName() {
        return new ArrayList<>(workerExecutions.keySet());
    }

    public void runTaskOnTarget(TaskTarget target, Task task) throws InterruptedException {
        System.out.println(target.getName() + " / " + target.getExecutionName() + ": DONE");

        /// implement task - compilation and simulation

        task.run(target);
        FinishedTargetDTO finishedTarget = new FinishedTargetDTO(target.getName(), target.getExecutionName(), target.getLogs(), this.name, FinishResultDTO.valueOf(target.getStatus().toString()), target.getProcessingTime());
        System.out.println("---------------------------------------------------------" + finishedTarget.toString());
        String finishedTargetAsString = GSON_INST.toJson(finishedTarget);
        RequestBody body = RequestBody.create(finishedTargetAsString, MediaType.parse("application/json"));

        HttpClientUtil.runAsyncWithBody(Constants.SEND_TARGET, body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
            }
        });

    }

    public void startRequestRefresher() {
        targetsRequestRefresher = new TargetsRequestRefresher(this, this::acceptTargets);
        targetsRequestTimer = new Timer();
        targetsRequestTimer.schedule(targetsRequestRefresher, TARGET_REQ_REFRESH_RATE, TARGET_REQ_REFRESH_RATE);
    }

    public void startLightRefresher() {
        lightWorkerExecutionRefresher = new LightWorkerExecutionRefresher(this::updateWorkerExecutions);
        lightWorkerExecTimer = new Timer();
        lightWorkerExecTimer.schedule(lightWorkerExecutionRefresher, CONTROL_REFRESH_RATE, CONTROL_REFRESH_RATE);
    }

    private void updateWorkerExecutions(List<LightWorkerExecution> lightWorkerExecutions) {
        lightWorkerExecutions.forEach(lightExec -> {
            synchronized (this) {
                WorkerExecution workerExecution = workerExecutions.get(lightExec.getName());
                workerExecution.setProgress(lightExec.getProgress());
                workerExecution.setWorkersCount(lightExec.getWorkers());
            }
        });
    }

    private void acceptTargets(List<NewExecutionTargetDTO> newTargets) {
        newTargets.forEach(t -> {
            TaskTarget target = new TaskTarget(t);
            target.setStatus(null);
            WorkerExecution workerExecution;
            synchronized (this) {
                workerExecution = workerExecutions.get(t.getExecutionName());
                target.setPayedPrice(workerExecution.getPrice());
                target.setType(workerExecution.getType());
                targets.add(target);
            }
            target.setStatus(TargetStatus.InProcess);
            Runnable r = () -> {
                System.out.println(Thread.currentThread().getName());
                try {
                    runTaskOnTarget(target, workerExecution.getTask());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Future<?> f = threadsExecutor.submit(r);
            futures.add(f);
        });
    }

    public Set<String> getWorkerExecutions() {
        return workerExecutions.keySet();
    }

    public void shutdown() throws InterruptedException {
        isAlive = false;
        if (targetsRequestRefresher != null && targetsRequestTimer != null) {
            threadsManager.join();
            System.out.println("die");
            targetsRequestRefresher.cancel();
            targetsRequestTimer.cancel();
        }

        waitAllThreadsToFinish();
        threadsExecutor.shutdownNow();
    }

    private void waitAllThreadsToFinish() {
        while(!AllThreadsDone(futures)){}
    }

    private boolean AllThreadsDone(List<Future<?>> futures) {
        boolean allDone = true;
        for (Future<?> future : futures) {
            allDone &= future.isDone(); // check if future is done
        }
        return allDone;
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

    public void setUnregisteredWorkerExecutionStatus(String taskName){
        workerExecutions.get(taskName).setExecutionStatus(WorkerExecutionStatus.Unregistered);
    }

    public void unregisterWorkerExecution(String taskName) {
        synchronized (this) {
            setUnregisteredWorkerExecutionStatus(taskName);
            workerExecutions.remove(taskName);
            try {
                unregisterTask(taskName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterTask(String taskName) throws IOException {
        List<String> oneTaskInListAsJson = new ArrayList<>();
        oneTaskInListAsJson.add(taskName);
        String taskAsJson = GSON_INST.toJson(oneTaskInListAsJson);
        RequestBody body = RequestBody.create(taskAsJson, MediaType.parse("application/json"));
        HttpClientUtil.runAsyncWithBody(UNREGISTER, body,new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
            }
        });

    }

    public ObservableList<WorkerExecution> getObservableWorkerExecutions() {
        return FXCollections.observableArrayList(workerExecutions.values());
    }

    public void pauseLightRefresher() {
        lightWorkerExecutionRefresher.pause();
    }

    public void resumeLightRefresher() {
        lightWorkerExecutionRefresher.resume();
    }


    public Collection<WorkerExecution> getExecution() {
        return workerExecutions.values();
    }

    public Map<String, WorkerExecution> getExecutionsMap() {
        return workerExecutions;
    }
}
