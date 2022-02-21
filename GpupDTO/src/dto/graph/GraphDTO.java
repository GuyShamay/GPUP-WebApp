package dto.graph;

import dto.target.TargetDTO;
import engine.graph.TargetGraph;
import engine.target.TargetType;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

public class GraphDTO {
    private String name;
    private String creatingUser;
    private int targetCount;
    private int independentCount;
    private int leafCount;
    private int middleCount;
    private int rootCount;
    private int simulationPrice;
    private int compilationPrice;
    private List<TargetDTO> targetsList;

    public GraphDTO(TargetGraph targetGraph) {
        this.name = targetGraph.getName();
        this.creatingUser = targetGraph.getCreatingUsername();
        this.independentCount = targetGraph.getTargetTypeCount(TargetType.Independent);
        this.leafCount = targetGraph.getTargetTypeCount(TargetType.Leaf);
        this.middleCount = targetGraph.getTargetTypeCount(TargetType.Middle);
        this.rootCount = targetGraph.getTargetTypeCount(TargetType.Root);
        targetCount = independentCount + leafCount + middleCount + rootCount;
        this.simulationPrice = targetGraph.getSimulationPrice();
        this.compilationPrice = targetGraph.getCompilationPrice();
        initTargets(targetGraph);
    }

    public GraphDTO() {
        this.name = null;
        this.creatingUser = null;
    }

    private void initTargets(TargetGraph targetGraph) {
        this.targetsList = targetGraph.getTargetsDTOList();
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

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatingUser() {
        return creatingUser;
    }

    public void setCreatingUser(String creatingUser) {
        this.creatingUser = creatingUser;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getIndependentCount() {
        return independentCount;
    }

    public void setIndependentCount(int independentCount) {
        this.independentCount = independentCount;
    }

    public int getLeafCount() {
        return leafCount;
    }

    public void setLeafCount(int leafCount) {
        this.leafCount = leafCount;
    }

    public int getMiddleCount() {
        return middleCount;
    }

    public void setMiddleCount(int middleCount) {
        this.middleCount = middleCount;
    }

    public int getRootCount() {
        return rootCount;
    }

    public void setRootCount(int rootCount) {
        this.rootCount = rootCount;
    }

    public void setTargetsList(List<TargetDTO> targetsList) {
        this.targetsList = targetsList;
    }

    public List<TargetDTO> getTargetsList() {
        return targetsList;
    }

    public List<String> getTargetsListByName(){
        List<String> list= new ArrayList<>();
        targetsList.forEach(targetDTO -> list.add(targetDTO.getName()));
        return list;
    }
}
