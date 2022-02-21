package old.dto;

import old.component.target.TargetType;
import old.component.targetgraph.TargetGraph;

public class TargetGraphDTO {
    private final int leaves;
    private final int independent;
    private final int middle;
    private final int root;
    private final int count;

    public int getLeaves() {
        return leaves;
    }

    public int getIndependent() {
        return independent;
    }

    public int getMiddle() {
        return middle;
    }

    public int getRoot() {
        return root;
    }

    public int getCount() {
        return count;
    }

    public TargetGraphDTO(TargetGraph targetGraph) {
        leaves = targetGraph.getSpecificTypeOfTargetsNum(TargetType.Leaf);
        independent = targetGraph.getSpecificTypeOfTargetsNum(TargetType.Independent);
        middle = targetGraph.getSpecificTypeOfTargetsNum(TargetType.Middle);
        root = targetGraph.getSpecificTypeOfTargetsNum(TargetType.Root);
        count = targetGraph.count();
    }

    @Override
    public String toString() {
return "There are "+count +" Target in the system.\n" +
        "Independent Targets: .... " + independent +
        "\nRoot Targets: ........... " + root +
        "\nMiddle Targets: ......... " + middle +
        "\nLeaf Targets: ........... " + leaves;
    }
}
