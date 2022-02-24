package old.component.targetgraph;

import engine.target.TargetsRelationType;
import engine.progressdata.ProgressData;
import engine.serialset.SerialSet;
import old.component.target.*;
import old.component.task.ProcessingType;
import old.dto.SerialSetDTO;
import old.dto.TargetDTO;
import old.dto.TargetInfoDTO;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class TargetGraph implements DirectableGraph, GraphActions {

    private final String name;
    private String workingDirectory;
    private Map<String, List<oldTarget>> dependsOnGraph;
    private Map<String, oldTarget> targetMap;
    private Map<String, List<oldTarget>> gTranspose;
    private Map<String, SerialSet> serialSets;

    public TargetGraph(String name) {
        this.name = name;
        dependsOnGraph = new HashMap<>();
        targetMap = new HashMap<>();
    }

    public TargetGraph() {
        this.name = null;
    }

    public void setSerialSets(Map<String, SerialSet> serialSets) {
        this.serialSets = serialSets;
        serialSets.forEach((s, serialSet) -> serialSet.getTargets().forEach(oldTarget::incrementSerialSetCounter));
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getName() {
        return name;
    }

    public void buildGraph(Map<String, oldTarget> targetList) {
        targetList.values().forEach(target -> {
            dependsOnGraph.put(target.getName(), new LinkedList<>());
            targetMap.put(target.getName(), target);
        });
        targetMap.values().forEach(target -> target.getDependsOnList().forEach(target1 -> addEdge(target.getName(), target1)));
        // updateLeavesAndIndependentsToWaiting();
    }

    public TargetGraph buildSubGraph(List<String> subTargetsList) {
        TargetGraph subTargetGraph = new TargetGraph();
        Map<String, List<oldTarget>> SubGraphDependsOn = new HashMap<>();
        Map<String, oldTarget> subTargetMap = new HashMap<>();

        subTargetsList.forEach(targetName -> {
            SubGraphDependsOn.put(targetName, new LinkedList<>());
            subTargetMap.put(targetName, targetMap.get(targetName));
        });

        subTargetGraph.setDependsOnList(SubGraphDependsOn);
        subTargetGraph.setTargetMap(subTargetMap);

        subTargetMap.values().forEach(target -> target.getDependsOnList().forEach(adj -> {
            if (subTargetsList.contains(adj.getName())) subTargetGraph.addEdge(target.getName(), adj);
        }));


        subTargetGraph.setSubGraphSerialSets(serialSets);
        return subTargetGraph;
    }

    private void setSubGraphSerialSets(Map<String, SerialSet> fullSerialSets) {
        serialSets = new HashMap<>();
        if (fullSerialSets != null && fullSerialSets.size() != 0) {
            fullSerialSets.forEach(((s, serialSet) -> {
                SerialSet serialSetCopy = new SerialSet(serialSet.getName(), serialSet.getTargetAsString());
                List<oldTarget> newTargets = new ArrayList<>();
                for (oldTarget t : serialSet.getTargets()) {
                    if (targetMap.containsKey(t.getName()))
                        newTargets.add(t);
                }
                if (!newTargets.isEmpty()) {
                    serialSetCopy.setTargets(newTargets);
                    serialSets.put(s, serialSetCopy);
                }
            }));
        }
    }


    private void setTargetMap(Map<String, oldTarget> subTargetMap) {
        targetMap = subTargetMap;
    }

    private void setDependsOnList(Map<String, List<oldTarget>> subDependsOnGraph) {
        dependsOnGraph = subDependsOnGraph;
    }

    @Override
    // This function return the count of targets
    public int count() {
        return targetMap.size();
    }

    @Override
    public void addEdge(String source, oldTarget destination) {
        if (dependsOnGraph.containsKey(source)) {

            //if the dest isn't in the graph already. so we add it
            if (!dependsOnGraph.containsKey(destination.getName()))
                addTarget(destination);

            // adding the destination to the adjacency list of the source
            dependsOnGraph.get(source).add(destination);
        }
    }

    @Override
    public void addTarget(oldTarget target) {
        dependsOnGraph.put(target.getName(), new LinkedList<>());
        targetMap.put(target.getName(), target);
    }

    @Override
    public boolean isDependOn(String s, String d) {
        return dependsOnGraph.get(s).stream().anyMatch(target -> target.getName().equals(d));
    }

    @Override
    public boolean isTargetExist(String t) {
        return targetMap.containsKey(t);
    }

    @Override
    public int getEdgesCount() {
        AtomicInteger count = new AtomicInteger();
        dependsOnGraph.values().forEach(list -> list.forEach(target -> count.getAndIncrement()));
        return count.get();
    }

    @Override
    public List<String> findPaths(String src, TargetsRelationType type, String dest) {
        List<String> paths = new ArrayList<>();
        Map<String, Boolean> isVisited = new HashMap<>();
        List<String> pathList = new ArrayList<>();

        targetMap.forEach((s, target) -> isVisited.put(s, false));

        if (type.equals(TargetsRelationType.RequiredFor)) {
            String tmp = src;
            src = dest;
            dest = tmp;
        }

        pathList.add(src);
        recFindPath(src, dest, isVisited, pathList, paths);
        if (type.equals(TargetsRelationType.RequiredFor)) {
            reversePathsList(paths);
        }

        return paths;
    }

    private void reversePathsList(List<String> paths) {

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < paths.size(); i++) {
            str.append(paths.get(i), 1, paths.get(i).length() - 1);
            str.reverse();
            paths.remove(i);
            paths.add(i, str.toString());
            str.setLength(0);
        }
    }

    private void recFindPath(String src, String dest, Map<String, Boolean> isVisited, List<String> localPath, List<String> paths) {
        if (src.equals(dest)) {
            paths.add(localPath.toString());
            return;
        }
        isVisited.put(src, true);
        for (oldTarget t : dependsOnGraph.get(src)) {

            if (!isVisited.get(t.getName())) {
                localPath.add(t.getName());
                recFindPath(t.getName(), dest, isVisited, localPath, paths);
                localPath.remove(t.getName());
            }
        }
        isVisited.put(src, false);
    }

    public int getSpecificTypeOfTargetsNum(TargetType targetType) {
        return (int) targetMap.values()
                .stream()
                .filter(target -> target.getType() == targetType)
                .count();
    }

    public TargetDTO getTargetInfo(String name) {
        if (targetMap.containsKey(name)) {
            return new TargetDTO(targetMap.get(name));
        } else {
            throw new NoSuchElementException("There is not a target named: " + name);
        }
    }

    @Override
    public void buildTransposeGraph() {
        if (gTranspose == null) {
            gTranspose = new HashMap<>();
            if (dependsOnGraph.size() != 0) {
                targetMap.forEach(((s, target) -> gTranspose.put(s, new ArrayList<>())));
                dependsOnGraph.forEach((s, targets) -> targets.forEach(target -> gTranspose.get(target.getName()).add(targetMap.get(s))));
            }
        }
    }

    @Override
    public List<String> findCircuit(String src) {
        Map<oldTarget, Boolean> isVisited = new HashMap<>();
        List<String> circuitList = new ArrayList<>();

        targetMap.forEach((s, target) -> isVisited.put(target, false));
        if (recDfsFindCircuitWithGivenTarget(isVisited, circuitList, targetMap.get(src), targetMap.get(src), false)) {
            return circuitList;
        }

        return null;
    }

    public List<oldTarget> getAllWaitingTargets() {
        List<oldTarget> resList = new ArrayList<>();
        targetMap.forEach(((s, target) -> {
            if (target.getRunResult().equals(RunResult.WAITING)) {
                resList.add(target);
            }
        }));
        return resList;
    }

    public void updateLeavesAndIndependentsToWaiting() {
        targetMap.forEach(((s, target) -> {
            if (target.getType() == TargetType.Leaf || target.getType() == TargetType.Independent) {
                target.setRunResult(RunResult.WAITING);
            }
        }));
    }

    public void updateTargetAdjAfterFinishWithoutFailure(ProgressData progressData, List<oldTarget> waitingList, oldTarget currentTarget) {
        gTranspose.get(currentTarget.getName()).forEach(target -> {
            if (target.getRunResult() != RunResult.FINISHED) {
                if (isAllAdjOfTargetFinished(target))
                    currentTarget.addToJustOpenedList(target);
                if (isAllAdjOfTargetFinishedWithoutFailure(target)) {
                    target.setRunResult(RunResult.WAITING);
                    //Platform.runLater(() -> progressData.move(RunResult.FROZEN, RunResult.WAITING, target.getName()));
                    if (!waitingList.contains(target)) {
                        waitingList.add(target);
                        target.setStartWaitingTime();
                    }
                }
            }
        });
    }

    public boolean isAllAdjOfTargetFinished(oldTarget target) {
        return dependsOnGraph.get(target.getName()).stream().allMatch(t -> (t.getRunResult().equals(RunResult.FINISHED)));
    }

    public boolean isAllAdjOfTargetFinishedWithoutFailure(oldTarget target) {
        if (isAllAdjOfTargetFinished(target)) {
            return dependsOnGraph.get(target.getName()).stream().allMatch(t -> (t.getFinishResult().equals(FinishResult.SUCCESS) || t.getFinishResult().equals(FinishResult.WARNING)));
        } else {
            return false;
        }
    }

    public void updateTargetAdjAfterFinishWithFailure(oldTarget currentTarget) {
        gTranspose.get(currentTarget.getName()).forEach(target -> {
            if (isAllAdjOfTargetFinished(target)) {
                currentTarget.addToJustOpenedList(target);
            }
        });
    }

    public void dfsTravelToUpdateSkippedList(oldTarget currentTarget) {
        Map<oldTarget, Boolean> isVisited = new HashMap<>();
        targetMap.forEach(((s, target) -> isVisited.put(target, false)));
        List<oldTarget> skippedList = currentTarget.getSkippedList();
        recDfsUpdateDependentsList(isVisited, skippedList, currentTarget);
        skippedList.remove(currentTarget);
    }

    private void recDfsUpdateDependentsList(Map<oldTarget, Boolean> isVisited, List<oldTarget> skippedList, oldTarget
            currentTarget) {
        skippedList.add(currentTarget);

        for (oldTarget t : gTranspose.get(currentTarget.getName())) {
            if (!isVisited.get(t)) {
                recDfsUpdateDependentsList(isVisited, skippedList, t);
            }
        }
        isVisited.replace(currentTarget, false, true);
    }

    private void updateTargetsFromScratch() {
        targetMap.forEach(((s, target) -> {
            target.setFinishResult(null);
            target.setRunResult(RunResult.FROZEN);
        }));
    }

    private void updateTargetIncremental() {
        targetMap.forEach(((s, target) -> {
            if (target.getRunResult().equals(RunResult.FINISHED)) {
                if (target.getFinishResult().equals(FinishResult.FAILURE)) {
                    if (isAllAdjOfTargetFinishedWithoutFailure(target)) {
                        target.setFinishResult(null);
                        target.setRunResult(RunResult.WAITING);
                    }
                }
            }
            if (target.getRunResult().equals(RunResult.SKIPPED))
                target.setRunResult(RunResult.FROZEN);
        }));
    }

    public void prepareGraphFromProcType(ProcessingType processingType) {
        switch (processingType) {
            case FromScratch:
                updateTargetsFromScratch();
                updateLeavesAndIndependentsToWaiting();
                break;
            case Incremental:
                updateTargetIncremental();
                break;
        }
    }

    public boolean allTargetsHaveRunResult() {
        boolean res = true;
        for (oldTarget t : targetMap.values()) {
            if (t.getRunResult() == RunResult.FROZEN) {
                res = false;
                break;
            }
        }
        return res;
    }

    public void clearJustOpenAndSkippedLists() {
        targetMap.forEach(((s, target) -> target.clearHelpingLists()));
    }

    private Boolean recDfsFindCircuitWithGivenTarget(Map<oldTarget, Boolean> isVisited, List<String> circuitList, oldTarget currentTarget, oldTarget src, Boolean foundCirc) {
        circuitList.add(currentTarget.getName());
        isVisited.replace(currentTarget, false, true);

        for (oldTarget t : dependsOnGraph.get(currentTarget.getName())) {
            if (t.equals(src)) {
                circuitList.add(src.getName());
                return true;
            }
            if (!isVisited.get(t)) {
                return recDfsFindCircuitWithGivenTarget(isVisited, circuitList, t, src, foundCirc);
            }
        }
        circuitList.remove(currentTarget.getName());
        return false;
    }

    public List<oldTarget> getWaitingAndFrozen() {
        List<oldTarget> waitingFrozen = new ArrayList<>();
        targetMap.forEach(((s, target) -> {
            if (target.getRunResult().equals(RunResult.WAITING) || target.getRunResult().equals(RunResult.FROZEN)) {
                waitingFrozen.add(target);
            }
        }));

        return waitingFrozen;
    }

    public List<TargetInfoDTO> getTargetsInfo() {
        List<TargetInfoDTO> list = new ArrayList<>();
        targetMap.forEach((s, target) -> list.add(new TargetInfoDTO(target)));
        return list;
    }

    public Set<String> getTargetsNamesList() {
        return targetMap.keySet();
    }

    public List<SerialSetDTO> getSerialSetInfo() {
        if (serialSets != null) {
            List<SerialSetDTO> list = new ArrayList<>();
            serialSets.forEach((s, serialSet) -> list.add(new SerialSetDTO(serialSet)));
            return list;
        }
        return null;
    }

    public List<oldTarget> getTargetsByRelation(String targetName, TargetsRelationType relationType) {
        oldTarget t = targetMap.get(targetName);
        List<oldTarget> targetsList = new ArrayList<>();
        switch (relationType) {
            case DependsOn:
                targetsList = t.getDependsOnList();
                break;
            case RequiredFor:
                targetsList = t.getRequiredForList();
                break;
        }
        return targetsList;
    }

    public void updateTargetsTypes() {
        dependsOnGraph.forEach((s, targets) -> {
            boolean isRequiredFor = isTargetRequiredForSomeone(s);
            if (targets.isEmpty() && !isRequiredFor)
                targetMap.get(s).setType(TargetType.Independent);
            else if (!targets.isEmpty() && isRequiredFor)
                targetMap.get(s).setType(TargetType.Middle);
            else if (targets.isEmpty())
                targetMap.get(s).setType(TargetType.Leaf);
            else
                targetMap.get(s).setType(TargetType.Root);
        });
    }

    private boolean isTargetRequiredForSomeone(String s) {
        boolean res = false;
        for (List<oldTarget> adjList : dependsOnGraph.values()) {
            if (adjList.contains(targetMap.get(s))) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void lockSerialSetOf(oldTarget currentTarget) {
        if (serialSets != null) {
            serialSets.values().forEach(serialSet -> {
                if (serialSet.contains(currentTarget))
                    serialSet.lockAll(currentTarget);
            });
        }
    }

    public void unlockSerialSetOf(oldTarget currentTarget) {
        if (serialSets != null) {
            serialSets.values().forEach(serialSet -> {
                if (serialSet.contains(currentTarget))
                    serialSet.unlockAll();
            });
        }
    }

    public boolean allTargetsFinished() {
        return targetMap.values().stream().allMatch(t -> (t.getRunResult().equals(RunResult.FINISHED)));
    }

    public Map<String, oldTarget> getTargetsMap() {
        return targetMap;
    }

    public void updateEachTargetDepListRecList() {

        dependsOnGraph.forEach((s, targets) -> {
            oldTarget t = targetMap.get(s);
            t.setRequiredFor(dfsTravelUpdateRelativesList(t, TargetsRelationType.RequiredFor));
            t.setDependOn(dfsTravelUpdateRelativesList(t, TargetsRelationType.DependsOn));
        });
    }

    private List<oldTarget> dfsTravelUpdateRelativesList(oldTarget t, TargetsRelationType relationType) {
        Map<oldTarget, Boolean> isVisited = new HashMap<>();
        List<oldTarget> list = new ArrayList<>();
        targetMap.forEach(((s, target) -> isVisited.put(target, false)));
        switch (relationType) {
            case DependsOn:
                recDfsUpdateImDependsOnList(isVisited, list, t);
                break;
            case RequiredFor:
                recDfsUpdateDependentsList(isVisited, list, t);
                break;
        }

        list.remove(t);
        return list;
    }

    private void recDfsUpdateImDependsOnList(Map<oldTarget, Boolean> isVisited, List<oldTarget> list, oldTarget currentTarget) {
        list.add(currentTarget);

        for (oldTarget t : dependsOnGraph.get(currentTarget.getName())) {
            if (!isVisited.get(t)) {
                recDfsUpdateImDependsOnList(isVisited, list, t);
            }
        }

        isVisited.replace(currentTarget, false, true);
    }

    public TargetInfoDTO getTargetInfoDTO(String name) {
        return new TargetInfoDTO(targetMap.get(name));
    }
}


