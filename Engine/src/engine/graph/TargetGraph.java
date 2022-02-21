package engine.graph;

import dto.execution.config.ExecutionConfigDTO;
import dto.target.TargetDTO;
import engine.target.RunResult;
import engine.target.Target;
import engine.target.TargetType;
import engine.target.TargetsRelationType;

import java.util.*;

public class TargetGraph implements Cloneable {
    private final String name;
    private final String creatingUsername;
    private int simulationPrice;
    private int compilationPrice;

    private Map<String, List<Target>> dependsOnGraph;
    private Map<String, Target> targetMap;
    private Map<String, List<Target>> gTranspose;

    public TargetGraph(String name, String creatingUsername) {
        this.name = name;
        this.creatingUsername = creatingUsername;
        dependsOnGraph = new HashMap<>();
        targetMap = new HashMap<>();
    }

    public TargetGraph() {
        this.name = null;
        this.creatingUsername = null;
    }


    //-----------------------------------------------------------------------------------------
    // Getters and Setters
    public Map<String, Target> getTargetMap() {
        return targetMap;
    }

    public String getCreatingUsername() {
        return creatingUsername;
    }

    public int getSimulationPrice() {
        return simulationPrice;
    }

    public void setSimulationPrice(int simulationPrice) {
        this.simulationPrice = simulationPrice;
    }

    public int getCompilationPrice() {
        return compilationPrice;
    }

    public void setCompilationPrice(int compilationPrice) {
        this.compilationPrice = compilationPrice;
    }

    public String getName() {
        return name;
    }

    //-----------------------------------------------------------------------------------------
    // Methods:
    public void addTargets(Map<String, Target> targetList) {
        targetList.values().forEach(target -> {
            dependsOnGraph.put(target.getName(), new LinkedList<>());
            targetMap.put(target.getName(), target);
        });
        targetMap.values().forEach(target -> target.getDependsOnList().forEach(target1 -> addEdge(target.getName(), target1)));

    }

    public void addEdge(String source, Target destination) {
        if (dependsOnGraph.containsKey(source)) {

            //if the dest isn't in the graph already. so we add it
            if (!dependsOnGraph.containsKey(destination.getName()))
                addTarget(destination);

            // adding the destination to the adjacency list of the source
            dependsOnGraph.get(source).add(destination);
        }
    }

    public void addTarget(Target target) {
        dependsOnGraph.put(target.getName(), new LinkedList<>());
        targetMap.put(target.getName(), target);
    }

    public boolean isDependOn(String s, String d) {
        return dependsOnGraph.get(s).stream().anyMatch(target -> target.getName().equals(d));
    }

    public boolean isTargetExist(String t) {
        return targetMap.containsKey(t);
    }

    public int getTargetTypeCount(TargetType targetType) {
        return (int) targetMap.values()
                .stream()
                .filter(target -> target.getType() == targetType)
                .count();
    }

    public void buildTransposeGraph() {
        if (gTranspose == null) {
            gTranspose = new HashMap<>();
            if (dependsOnGraph.size() != 0) {
                targetMap.forEach(((s, target) -> gTranspose.put(s, new ArrayList<>())));
                dependsOnGraph.forEach((s, targets) -> targets.forEach(target -> gTranspose.get(target.getName()).add(targetMap.get(s))));
            }
        }
    }

    public List<String> getTargetsListByName() {
        List<String> list = new ArrayList<>();
        targetMap.keySet().forEach(s -> list.add(s));
        return list;
    }

    public List<TargetDTO> getTargetsDTOList() {
        List<TargetDTO> list = new ArrayList<>();
        targetMap.values().forEach(target -> list.add(new TargetDTO(target)));
        return list;
    }

    public TargetGraph clone(ExecutionConfigDTO config) {
        TargetGraph newGraph = new TargetGraph(this.name, this.creatingUsername);
        newGraph.setCompilationPrice(this.compilationPrice);
        newGraph.setSimulationPrice(this.simulationPrice);
        Map<String, Target> newTargetsMap;

        if (config.isAllTargets()) {
            newTargetsMap = TargetGraphUtil.copyOfAllTargets(this.targetMap);
        } else if (config.isCustomTargets()) {
            newTargetsMap = TargetGraphUtil.copyOfCustomTargets(config.getCustomTargetsList(), this.targetMap);
        } else if (config.isWhatIfTarget()) {
            newTargetsMap = TargetGraphUtil.copyOfWhatIf(this, config.getWhatIfTargetName(), TargetsRelationType.valueOf(config.getWhatIfTargetRelation().name()));
        } else {
            throw new RuntimeException("Failure: Problem with Execution Configuration");
        }
        TargetGraphUtil.updateDependencies(this.targetMap, newTargetsMap);
        TargetGraphUtil.updateTargetsType(newTargetsMap);
        newGraph.addTargets(newTargetsMap);
        return newGraph;
    }

    public TargetGraph clone() {
        TargetGraph newGraph = new TargetGraph(this.name, this.creatingUsername);

        newGraph.setCompilationPrice(this.compilationPrice);
        newGraph.setSimulationPrice(this.simulationPrice);
        Map<String, Target> newTargetsMap = TargetGraphUtil.copyOfAllTargets(this.targetMap);
        TargetGraphUtil.updateDependencies(this.targetMap, newTargetsMap);
        TargetGraphUtil.updateTargetsType(newTargetsMap);
        newGraph.addTargets(newTargetsMap);
        return newGraph;
    }

    public void fromScratchReset() {
        targetMap.values().forEach(target -> {
            target.setRunResult(RunResult.FROZEN);
            target.setFinishResult(null);
        });
    }

    public int getCount() {
        return targetMap.size();
    }

    public void checkValidIncremental() {
        if (targetMap.values().stream().filter(target -> !target.getRunResult().equals(RunResult.FINISHED)).count() == 0) {
            // all targets finished
            fromScratchReset();
        }
    }

    //-----------------------------------------------------------------------------------------

    // Find Circuit

    public boolean hasCircuit() {
        return targetMap.keySet().stream().anyMatch(s -> findCircuit(s) != null);
    }

    public List<String> findCircuit(String src) {
        Map<Target, Boolean> isVisited = new HashMap<>();
        List<String> circuitList = new ArrayList<>();

        targetMap.forEach((s, target) -> isVisited.put(target, false));
        if (recDfsFindCircuitFromGivenTarget(isVisited, circuitList, targetMap.get(src), targetMap.get(src), false)) {
            return circuitList;
        }

        return null;
    }

    private Boolean recDfsFindCircuitFromGivenTarget(Map<Target, Boolean> isVisited, List<String> circuitList, Target currentTarget, Target src, Boolean foundCirc) {
        circuitList.add(currentTarget.getName());
        isVisited.replace(currentTarget, false, true);

        for (Target t : dependsOnGraph.get(currentTarget.getName())) {
            if (t.equals(src)) {
                circuitList.add(src.getName());
                return true;
            }
            if (!isVisited.get(t)) {
                if (recDfsFindCircuitFromGivenTarget(isVisited, circuitList, t, src, foundCirc)) {
                    return true;
                }
            }
        }
        circuitList.remove(currentTarget.getName());
        return false;
    }

    // -----------------------------------------------------------------------------------------

    // Find Path

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

        if (paths.isEmpty()) {
            if (type.equals(TargetsRelationType.RequiredFor)) {
                paths.add("No Paths from " + dest + " to " + src + " were found.");
            } else {
                paths.add("No Paths from " + src + " to " + dest + " were found.");
            }
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
        for (Target t : dependsOnGraph.get(src)) {

            if (!isVisited.get(t.getName())) {
                localPath.add(t.getName());
                recFindPath(t.getName(), dest, isVisited, localPath, paths);
                localPath.remove(t.getName());
            }
        }
        isVisited.put(src, false);
    }

    // -----------------------------------------------------------------------------------------

    // What If

    public List<TargetDTO> whatIfDTO(String src, TargetsRelationType type) {
        List<Target> list = whatIf(src, type);

        List<TargetDTO> dtoList = new ArrayList<>();
        list.forEach(target -> dtoList.add(new TargetDTO(target)));
        return dtoList;
    }

    public List<Target> whatIf(String src, TargetsRelationType type) {
        List<Target> list = new ArrayList<>();
        if (targetMap.containsKey(src)) {
            whatIfRecursive(list, targetMap.get(src), type);
        } else {
            return null;
        }
        return list;
    }

    private void whatIfRecursive(List<Target> list, Target src, TargetsRelationType type) {
        if (type == TargetsRelationType.RequiredFor) {
            if (src.getRequiredForList() == null || src.getRequiredForList().isEmpty()) {
                return;
            }
            for (Target target : src.getRequiredForList()) {
                if (!list.contains(target)) {
                    list.add(target);
                }
                whatIfRecursive(list, target, type);
            }
        } else {
            if (src.getDependsOnList() == null || src.getDependsOnList().isEmpty()) {
                return;
            }
            for (Target target : src.getDependsOnList()) {
                if (!list.contains(target)) {
                    list.add(target);
                }
                whatIfRecursive(list, target, type);
            }
        }
    }

    //----------------------------------------------------------------------------------------
}
