package engine.serialset;

import old.component.target.oldTarget;

import java.util.List;

public class SerialSet {
    private final String name;
    private final String targetAsString;
    private List<oldTarget> targets;

    public SerialSet(String name, String targetAsString) {
        this.name = name;
        this.targetAsString = targetAsString;
    }

    public String getTargetAsString() {
        return targetAsString;
    }

    public void setTargets(List<oldTarget> targets) {
        this.targets = targets;
    }

    public List<oldTarget> getTargets() {
        return targets;
    }

    public String getName() {
        return name;
    }

    public void lockAll(oldTarget lockingTarget) {
        targets.stream().filter(target -> !(target.equals(lockingTarget))).forEach(oldTarget::lock);
    }

    public void unlockAll(){
        targets.forEach(oldTarget::unlock);
    }

    public boolean contains(oldTarget currentTarget) {
        return targets.contains(currentTarget);
    }
}
