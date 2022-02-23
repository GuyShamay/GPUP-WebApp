package old.engine;

import engine.target.TargetsRelationType;
import old.component.target.*;
import old.component.targetgraph.TargetGraph;
import old.component.task.ProcessingType;
import old.component.task.RunTask;
import old.component.task.Task;
import old.component.task.compile.CompileTask;
import old.component.task.config.CompileConfig;
import old.component.task.config.SimulationConfig;
import old.component.task.config.TaskConfig;
import old.component.task.simulation.ProcessingTimeType;
import old.component.task.simulation.SimulationTask;
import old.dto.*;
import old.exception.ElementExistException;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

// IMPLEMENT INTERFACE
public class GPUPEngine implements Engine {
    private TargetGraph targetGraph;
    private Task task; // can it run several tasks?
    private ProcessingType processingType;
    private int maxParallelism;
    private TargetGraph subTargetGraph;
    private RunTask runTask;

    /*private void loadXmlToTargetGraph(String path) throws FileNotFoundException, JAXBException, ElementExistException {
        final String PACKAGE_NAME = "jaxb.generated";
        GPUPDescriptor gpupDescriptor;
        InputStream inputStream = new FileInputStream(path);
        JAXBContext jc = JAXBContext.newInstance(PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        gpupDescriptor = (GPUPDescriptor) u.unmarshal(inputStream);
        targetGraph = GPUPParser.parseTargetGraph(gpupDescriptor);
    }*/

    @Override
    public void buildGraphFromXml(String path) throws JAXBException, FileNotFoundException, ElementExistException {
       // loadXmlToTargetGraph(path);
    }

    @Override
    public void buildGraphFromXml(File file) throws JAXBException, FileNotFoundException, ElementExistException {
        //loadFileToGraph(file);
        if (!targetGraph.getTargetsMap().keySet().stream().anyMatch(s -> targetGraph.findCircuit(s) != null)) {
            targetGraph.buildTransposeGraph();
            targetGraph.updateEachTargetDepListRecList();
        }
    }

   /* private void loadFileToGraph(File file) throws FileNotFoundException, JAXBException {
        final String PACKAGE_NAME = "jaxb.generated.v2";
        GPUPDescriptor gpupDescriptor;

        InputStream inputStream = new FileInputStream(file);
        JAXBContext jc = JAXBContext.newInstance(PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        gpupDescriptor = (GPUPDescriptor) u.unmarshal(inputStream);
        targetGraph = GPUPParser.parseTargetGraph(gpupDescriptor);
        maxParallelism = GPUPParser.getMaxParallelism(gpupDescriptor);
    }*/

    @Override
    public TargetDTO getTargetInfo(String name) {
        if (targetGraph.isTargetExist(name)) {
            return targetGraph.getTargetInfo(name);
        }
        throw new NoSuchElementException("There is not a target named: " + name + "\n");
    }

    @Override
    public TargetGraphDTO getGraphInfo() {
        return new TargetGraphDTO(targetGraph);
    }

    @Override
    public boolean isInitialized() {
        return targetGraph != null;
    }

    //Before Task-run methods

    @Override
    public void initTaskGPUP1(int targetProcessingTimeMs, int taskProcessingTimeType, float successProb, float successWithWarningsProb, ProcessingType status) {
        ProcessingTimeType procTimeType = taskProcessingTimeType == 1 ? ProcessingTimeType.Random : ProcessingTimeType.Permanent;
        task = new SimulationTask(targetGraph.getName(), procTimeType, successProb, successWithWarningsProb, targetProcessingTimeMs);
        targetGraph.buildTransposeGraph();
    }

    public void initTask(TaskConfig taskConfig) {
        processingType = taskConfig.getProcessingType();
        switch (taskConfig.getTaskType()) {
            case Simulation:
                task = new SimulationTask((SimulationConfig) taskConfig.getConfig(), taskConfig.getThreadsParallelism());
                break;
            case Compilation:
                task = new CompileTask((CompileConfig) taskConfig.getConfig(), taskConfig.getThreadsParallelism());
                break;
        }

        runTask = new RunTask(task);
        initGraphForRun(taskConfig);
    }

    private void initGraphForRun(TaskConfig taskConfig) {
        subTargetGraph = createSubTargetGraph(taskConfig);
        if (!isCircuit()) {
            runTask.setSubTargetGraph(subTargetGraph);
            runTask.initProgressData(processingType);
            subTargetGraph.updateTargetsTypes();
            subTargetGraph.buildTransposeGraph();
            subTargetGraph.updateEachTargetDepListRecList();
            //NEEDED??
            task.updateRelevantTargets(subTargetGraph.getWaitingAndFrozen());
            ///
            subTargetGraph.clearJustOpenAndSkippedLists();
            updateRunAndFinishResults();
        }
    }

    //NOT IN USE
    private void updateRunAndFinishResults() {
        resetEveryOne();
        updateLeavesAndIndependentsToWaiting();
    }

    public void updateLeavesAndIndependentsToWaiting() {
        subTargetGraph.getTargetsMap().forEach(((s, target) -> {
            if (target.getType() == TargetType.Leaf || target.getType() == TargetType.Independent) {
                target.setRunResult(RunResult.WAITING);
                // runTask.getProgressData().move(RunResult.FROZEN, RunResult.WAITING, target.getName());
            }
        }));
    }

    //Everyone null and Frozen.
    private void resetEveryOne() {
        subTargetGraph.getTargetsMap().forEach(((s, target) -> {
            target.setFinishResult(null);
            target.setRunResult(RunResult.FROZEN);
            //already frozen in UI
        }));
    }

    private void correctProcessingType(List<String> choosenTargets) {
        if (allTargetsFinished(choosenTargets) && processingType == ProcessingType.Incremental) {
            System.out.println("Task will run 'From Scrach'.");
            processingType = ProcessingType.FromScratch;
        }
    }

    private boolean allTargetsFinished(List<String> choosenTargets) {
        for (String s : choosenTargets) {
            if (!hasGoodRunResult(s))
                return false;
        }
        return true;
    }

    private TargetGraph createSubTargetGraph(TaskConfig taskConfig) {
        ///List Of The Targets User Asked To Run
        List<String> choosenTargets = getChoosenTargets(taskConfig);
        correctProcessingType(choosenTargets);
        //Updated List after incremental if needed
        correctListByProcType(choosenTargets);
        //subTargetGraph contains all and only relevant targets.
        subTargetGraph = targetGraph.buildSubGraph(choosenTargets);

        return subTargetGraph;
    }

    private void correctListByProcType(List<String> chosenTargets) {
        if (processingType.equals(ProcessingType.Incremental)) {
            chosenTargets.removeIf(this::hasGoodRunResult);
        }
    }

    private boolean hasGoodRunResult(String s) {
        Target t = targetGraph.getTargetsMap().get(s);
        if (t.getRunResult() == RunResult.FINISHED && (t.getFinishResult() == FinishResult.SUCCESS || t.getFinishResult() == FinishResult.WARNING)) {
            return true;
        }
        return false;
    }

    private List<String> getChoosenTargets(TaskConfig taskConfig) {
        List<String> choosenTargets = new ArrayList<>();

        if (taskConfig.isAllTargets()) {
            for (String t : targetGraph.getTargetsMap().keySet()) {
                choosenTargets.add(t);
            }
        } else if (taskConfig.getCustomTargets() != null) {
            choosenTargets = taskConfig.getCustomTargets();
        } else {
            choosenTargets = getWhatIfSubTargetsList(taskConfig.getWhatIfTarget(), taskConfig.getWhatIfRelation());
        }

        return choosenTargets;
    }

    private List<String> getWhatIfSubTargetsList(String whatIfTarget, TargetsRelationType whatIfRelation) {
        List<String> res = new ArrayList<>();
        targetGraph.getTargetsByRelation(whatIfTarget, whatIfRelation).forEach(target -> res.add(target.getName()));
        return res;
    }

    @Override
    public void setProcessingType(ProcessingType status) {
        this.processingType = status;
    }

    public RunTask getCurrTask() {
        return runTask;
    }

    public ObservableList<String> getList(String type) {
        switch (type) {
            case "frozen":
                return runTask.getProgressData().getFrozen();
            case "waiting":
                return runTask.getProgressData().getWaiting();
            case "inProcess":
                return runTask.getProgressData().getInprocces();
            case "skipped":
                return runTask.getProgressData().getSkipped();
            case "success":
                return runTask.getProgressData().getSuccess();
            case "warnings":
                return runTask.getProgressData().getWarning();
            case "failure":
                return runTask.getProgressData().getFailure();
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////

    @Override
    public void resume() {
        runTask.resume();
    }

    @Override
    public void pause() {
        runTask.pause();
    }

    @Override
    public PathsDTO findPaths(String src, String dest, TargetsRelationType type) {
        if (!src.equals(dest)) {
            if (targetGraph.isTargetExist(src) && targetGraph.isTargetExist(dest)) {
                return new PathsDTO(targetGraph.findPaths(src, type, dest), src, dest, type);
            } else {
                throw new NoSuchElementException("The required targets aren't exist");
            }
        } else {
            throw new RuntimeException("The targets you entered are the same.");
        }
    }

    private String createDirectoryName() {
        String path = targetGraph.getWorkingDirectory() + "/";
        String creationTime = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss").format(Calendar.getInstance().getTime());
        path = path + targetGraph.getName() + " - " + creationTime;
        return path;

    }

    public void createTaskDirectory(String path) {
        File taskDirectory = new File(path);
        if (!taskDirectory.exists()) {
            if (!taskDirectory.mkdir()) {
                throw new RuntimeException("Failure with creating the Task's Directory");
            }
        }
    }

    @Override
    public boolean isFirstTaskRun() {
        return task == null;
    }

    @Override
    public CircuitDTO findCircuit(String targetName) {
        if (targetGraph.isTargetExist(targetName)) {
            return new CircuitDTO(targetGraph.findCircuit(targetName));
        } else {
            throw new NoSuchElementException("Target " + targetName + " doesn't exist.");
        }
    }

    @Override
    public List<TargetInfoDTO> getTargetsInfo() {
        return targetGraph.getTargetsInfo();
    }

    private void writeTargetToFile(Instant start, Instant end, GPUPConsumer target, String path) throws IOException {
        String fileName = target.getName() + ".log";
        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path + "/" + fileName)))) {

            // write to file:
            out.write(start.toString());
            out.write(target.toString());
            out.write(end.toString());
        }
    }

    public Set<String> getAllTargetsByName() {
        return targetGraph.getTargetsNamesList();
    }

    @Override
    public List<SerialSetDTO> getSerialSetInfo() {
        return targetGraph.getSerialSetInfo();
    }

    public List<TargetInfoDTO> getTargetsByRelation(String targetName, TargetsRelationType relationType) {
        List<Target> targetsList = targetGraph.getTargetsByRelation(targetName, relationType);
        List<TargetInfoDTO> targetsDTOList = new ArrayList<>();
        targetsList.forEach((target -> targetsDTOList.add(new TargetInfoDTO(target))));
        return targetsDTOList;
    }

    @Override
    public int getMaxParallelism() {
        return maxParallelism;
    }

    public void runTaskGPUP1(Consumer<GPUPConsumer> consumer) throws InterruptedException {
        Instant totalStart, totalEnd, start, end;
        List<Target> waitingList;

        targetGraph.prepareGraphFromProcType(processingType);
        task.updateRelevantTargets(targetGraph.getWaitingAndFrozen());
        targetGraph.clearJustOpenAndSkippedLists();
        waitingList = targetGraph.getAllWaitingTargets();

        totalStart = Instant.now();
        String dirPath = createDirectoryName();
        createTaskDirectory(dirPath);
        //  task.setDirectoryPath(dirPath);

        if (waitingList.isEmpty() && processingType.equals(ProcessingType.Incremental)) {
            throw new RuntimeException("The graph already had been processed completely, there is no need for 'Incremental' action");
        }

        while (!waitingList.isEmpty()) {
            start = Instant.now();
            Target currentTarget = waitingList.remove(0);
            currentTarget.setRunResult(RunResult.INPROCESS);

            currentTarget.setFinishResult(task.run(currentTarget.getName(), currentTarget.getUserData()));
            currentTarget.setRunResult(RunResult.FINISHED);

            if (currentTarget.getFinishResult() == FinishResult.FAILURE) {
                targetGraph.dfsTravelToUpdateSkippedList(currentTarget);
                targetGraph.updateTargetAdjAfterFinishWithFailure(currentTarget);
            } else {
                // targetGraph.updateTargetAdjAfterFinishWithoutFailure(progressData, waitingList, currentTarget);
            }
            end = Instant.now();
            currentTarget.setTaskRunDuration(Duration.between(start, end));

            GPUPConsumer consumerDTO = new ProcessedTargetDTO(currentTarget);
            consumerDTO.setTaskOutput(new SimulationOutputDTO(task.getProcessingTime()));
            //Writing to file
            //  writeTargetToFile(start, end, consumerDTO, task.getDirectoryPath());
            // Writing to console
            consumer.accept(consumerDTO);
        }

        totalEnd = Instant.now();
        Duration totalRunDuration = Duration.between(totalStart, totalEnd);
        //StatisticsDTO statisticsDTO = calcStatistics(totalRunDuration);
        //consumer.accept(statisticsDTO);
    }

    public int getTargetsCount() {
        return targetGraph.count();
    }

    public int getSpecificTypeOfTargetsNum(TargetType targetType) {
        return targetGraph.getSpecificTypeOfTargetsNum(targetType);
    }

    @Override
    public TargetInfoDTO getTargetInfoDTO(String name) {
        return subTargetGraph.getTargetInfoDTO(name);
    }

    @Override
    public void increaseThreadsNum(Integer newVal) {
        task.incParallelism(newVal);
    }

    @Override
    public boolean isCircuit() { // no need
        if (subTargetGraph.count() != 0) {
            return subTargetGraph.getTargetsMap().keySet().stream().anyMatch(s -> targetGraph.findCircuit(s) != null);
        }
        return false;
    }

    @Override
    public Set<String> getCurrentTaskTargetByName() {
        return subTargetGraph.getTargetsNamesList();
    }

}
