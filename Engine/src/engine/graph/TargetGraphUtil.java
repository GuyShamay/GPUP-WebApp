package engine.graph;

import engine.target.Target;
import engine.target.TargetType;
import engine.target.TargetsRelationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TargetGraphUtil {
    public static Map<String, Target> copyOfAllTargets(Map<String, Target> targetsMap) {
        Map<String, Target> newTargetsMap = new HashMap<>();
        targetsMap.forEach((s, target) -> {
            try {
                newTargetsMap.put(s, target.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        return newTargetsMap;
    }

    public static Map<String, Target> copyOfCustomTargets(List<String> list, Map<String, Target> targetsMap) {
        Map<String, Target> newTargetsMap = new HashMap<>();
        targetsMap.forEach((s, target) -> {
            if (list.contains(s)) {
                try {
                    newTargetsMap.put(s, target.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });
        return newTargetsMap;
    }

    public static Map<String, Target> copyOfWhatIf(TargetGraph baseGraph, String src, TargetsRelationType type) {
        Map<String, Target> newTargetsMap = new HashMap<>();
        List<Target> list = baseGraph.whatIf(src, type);
        list.forEach(target -> {
            try {
                newTargetsMap.put(target.getName(), target.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        return newTargetsMap;
    }

    public static void updateDependencies(Map<String, Target> oldMap, Map<String, Target> newMap) {

        newMap.forEach((s, newTarget) -> oldMap.get(s).getDependsOnList().forEach(oldTarget -> {
            if (newMap.containsKey(oldTarget.getName())) {
                newTarget.addDependOnTarget(newMap.get(oldTarget.getName()));
            }
        }));
        newMap.forEach((s, newTarget) -> oldMap.get(s).getRequiredForList().forEach(oldTarget -> {
            if (newMap.containsKey(oldTarget.getName())) {
                newTarget.addRequiredForTarget(newMap.get(oldTarget.getName()));
            }
        }));
    }

    public static void updateTargetsType(Map<String, Target> targets) {
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
}
