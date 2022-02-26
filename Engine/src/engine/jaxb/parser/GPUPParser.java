package engine.jaxb.parser;

import engine.graph.TargetGraph;
import engine.jaxb.generated.v3.*;
import engine.serialset.SerialSet;
import engine.target.Target;
import engine.target.TargetType;


import java.util.*;

public abstract class GPUPParser {

    private static final String SIMULATION = "Simulation";
    private static final String COMPILATION = "Compilation";

    public static TargetGraph parseGraph(GPUPDescriptor gpupDescriptor, String cretingUser) {
        TargetGraph targetGraph = null;
        Map<String, Target> targetMap = null;

        String name = gpupDescriptor.getGPUPConfiguration().getGPUPGraphName();

        targetGraph = new TargetGraph(name, cretingUser);
        targetMap = parseTargetList(gpupDescriptor.getGPUPTargets());
        parsePricing(targetGraph, gpupDescriptor);
        targetGraph.addTargets(targetMap);
        if (gpupDescriptor.getGPUPSerialSets() != null) {
           //targetGraph.setSerialSets(parseSerialSets(gpupDescriptor, targetMap));
        }
        return targetGraph;
    }

    private static void parsePricing(TargetGraph targetGraph, GPUPDescriptor gpupDescriptor) {
        List<GPUPConfiguration.GPUPPricing.GPUPTask> tasksPricing = gpupDescriptor.getGPUPConfiguration().getGPUPPricing().getGPUPTask();
        for (GPUPConfiguration.GPUPPricing.GPUPTask gpupTask : tasksPricing) {
            if (gpupTask.getName().equals(SIMULATION)) {
                targetGraph.setSimulationPrice(gpupTask.getPricePerTarget());
            } else if (gpupTask.getName().equals(COMPILATION)) {
                targetGraph.setCompilationPrice(gpupTask.getPricePerTarget());
            }
        }
    }

    /*public static TargetGraph parseTargetGraph(GPUPDescriptor gpupDescriptor) throws ElementExistException, NoSuchElementException {
        TargetGraph targetGraph = null;
        Map<String, Target> targetMap = null;
        String name = gpupDescriptor.getGPUPConfiguration().getGPUPGraphName();
        String workingDirectory = gpupDescriptor.getGPUPConfiguration().getGPUPWorkingDirectory();

        targetGraph = new TargetGraph(name);
        targetGraph.setWorkingDirectory(workingDirectory);
        targetMap = parseTargetList(gpupDescriptor.getGPUPTargets());
        targetGraph.buildGraph(targetMap);
        if (gpupDescriptor.getGPUPSerialSets() != null) {
            targetGraph.setSerialSets(parseSerialSets(gpupDescriptor, targetMap));
        }
        return targetGraph;
    }*/

    /*private static Map<String, SerialSet> parseSerialSets(GPUPDescriptor gpupDescriptor, Map<String, Target> targetMap) {
        Map<String, SerialSet> serialSetMap = new HashMap<>();
        List<GPUPDescriptor.GPUPSerialSets.GPUPSerialSet> gpupSerialSets = gpupDescriptor.getGPUPSerialSets().getGPUPSerialSet();
        gpupSerialSets.forEach(gpupSerialSet -> {
            if (!serialSetMap.containsKey(gpupSerialSet.getName())) {
                String name = gpupSerialSet.getName();
                SerialSet serialSet = parseSerialSet(gpupSerialSet, targetMap); // could throw exception
                serialSetMap.put(name, serialSet);
            } else {
                throw new ElementExistException("Serial Set", gpupSerialSet.getName());
            }
        });

        return serialSetMap;
    }*/

    /*private static SerialSet parseSerialSet(GPUPDescriptor.GPUPSerialSets.GPUPSerialSet gpupSerialSet, Map<String, Target> targetMap) {
        String targetsByString = gpupSerialSet.getTargets();
        String name = gpupSerialSet.getName();
        SerialSet serialSet = new SerialSet(name, gpupSerialSet.getTargets());
        try {
            List<Target> targets = parseSerialSetFromString(targetsByString, targetMap);
            targets.forEach(target -> target.addToSerialSetsList(name));
            serialSet.setTargets(targets);
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("In serial set " + name + "\n" + ex.getMessage());
        }
        return serialSet;
    }*/

    private static List<Target> parseSerialSetFromString(String targetsByString, Map<String, Target> targetMap) {
        List<Target> targets = new ArrayList<>();
        String[] res = targetsByString.split("[,]", 0);
        for (String targetStr : res) {
            if (targetMap.containsKey(targetStr)) {
                targets.add(targetMap.get(targetStr));
            } else {
                throw new NoSuchElementException(targetStr + " isn't a target in the graph.");
            }
        }
        return targets;
    }

    /*public static int getMaxParallelism(GPUPDescriptor gpupDescriptor) {
        return gpupDescriptor.getGPUPConfiguration().getGPUPMaxParallelism();
    }*/

    private static Target createTarget(GPUPTarget gpupTarget) {
        String name = gpupTarget.getName();
        Target target = new Target(name);

        target.setUserData(gpupTarget.getGPUPUserData());

        return target;
    }

    private static Map<String, Target> parseTargetList(GPUPTargets gpupTargets) {
        Map<String, Target> targets = new HashMap<>();

        // creating a map of targets (no dependencies yet):
        for (GPUPTarget gt : gpupTargets.getGPUPTarget()) {
            if (!targets.containsKey(gt.getName())) {
                Target target = createTarget(gt);
                targets.put(target.getName(), target);
            } else {
                throw new RuntimeException(gt.getName() + " Exist");
            }
        }
        // update the dependencies of each target:
        updateDependencies(targets, gpupTargets);
        updateTargetsType(targets);
        return targets;
    }

    private static void updateDependencies(Map<String, Target> targets, GPUPTargets gpupTargets) throws NoSuchElementException {
        final String DEPENDS_ON = "dependsOn";
        final String REQUIRED_FOR = "requiredFor";

        for (GPUPTarget gt : gpupTargets.getGPUPTarget()) {
            Target target = targets.get(gt.getName());
            if (gt.getGPUPTargetDependencies() != null) {
                for (GPUPTargetDependencies.GPUGDependency dependency : gt.getGPUPTargetDependencies().getGPUGDependency()) {

                    if (validDependency(dependency, gt.getName()) && targets.containsKey(dependency.getValue())) {
                        if (dependency.getType().equals(DEPENDS_ON)) {
                            target.addDependOnTarget(targets.get(dependency.getValue()));
                            targets.get(dependency.getValue()).addRequiredForTarget(target);
                        } else if (dependency.getType().equals(REQUIRED_FOR)) {
                            target.addRequiredForTarget(targets.get(dependency.getValue()));
                            targets.get(dependency.getValue()).addDependOnTarget(target);
                        }

                        // check for 2-way circle:
                        if (!isTwoWayCircle(target, targets.get(dependency.getValue()), dependency.getType())) {
                            throw new NoSuchElementException("There is a conflict between targets: " + target.getName() + ", " + dependency.getValue());
                        }
                    } else {
                        throw new NoSuchElementException("There was a try to add dependency of a non-existing target");
                    }
                }
            }
        }
    }

    private static void updateTargetsType(Map<String, Target> targets) {

        for (Target target : targets.values()) {
            if (target.getDependsOnList().size() == 0 && target.getRequiredForList().size() == 0) {
                target.setType(TargetType.Independent);
            } else if (target.getDependsOnList().size() != 0 && target.getRequiredForList().size() != 0) {
                target.setType(TargetType.Middle);
            } else if (target.getRequiredForList().size() == 0) {
                target.setType(TargetType.Root);
            } else {
                target.setType(TargetType.Leaf);
            }
        }
    }

    private static boolean isTwoWayCircle(Target target1, Target target2, String type) {
        if (target1.isDependency(target2, type) && target2.isDependency(target1, type)) {
            return false;
        }
        return true;
    }

    private static boolean validDependency(GPUPTargetDependencies.GPUGDependency dependency, String name) {
        if (dependency.getType() == null || dependency.getValue() == null || dependency.getValue() == name) {
            throw new NoSuchElementException("One of the dependencies of " + name + " is wrong.");
        }
        return true;
    }
}
