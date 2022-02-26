package engine.serialset;

import engine.target.Target;

import java.util.List;

public class SerialSet {
    private final String name;
    private final String targetAsString;
    private List<Target> targets;

    public SerialSet(String name, String targetAsString) {
        this.name = name;
        this.targetAsString = targetAsString;
    }

    public String getTargetAsString() {
        return targetAsString;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public String getName() {
        return name;
    }


    public boolean contains(Target currentTarget) {
        return targets.contains(currentTarget);
    }
}
