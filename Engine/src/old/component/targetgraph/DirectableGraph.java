package old.component.targetgraph;

import java.util.List;

public interface DirectableGraph {

    void buildTransposeGraph();

    List<String> findCircuit(String src);

}