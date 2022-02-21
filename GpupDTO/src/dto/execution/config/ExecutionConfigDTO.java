package dto.execution.config;

import dto.util.DTOUtil.RelationTypeDTO;
import dto.util.DTOUtil.ExecutionTypeDTO;

import java.util.List;

public class ExecutionConfigDTO {
    private String name;
    private String creatingUser;
    private String graphName;
    private List<String> customTargetsList;
    private boolean isAllTargets = false;
    private boolean isCustomTargets = false;
    private boolean isWhatIfTarget = false;
    private String whatIfTargetName;
    private RelationTypeDTO whatIfTargetRelation;
    private ConfigDTO configDTO;
    private ExecutionTypeDTO executionType;
    private int price;

    public boolean isCustomTargets() {
        return isCustomTargets;
    }

    public void setCustomTargets(boolean customTargets) {
        isCustomTargets = customTargets;
    }

    public boolean isWhatIfTarget() {
        return isWhatIfTarget;
    }

    public void setWhatIfTarget(boolean whatIfTarget) {
        isWhatIfTarget = whatIfTarget;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String name) {
        this.graphName = name;
    }

    public String getCreatingUser() {
        return creatingUser;
    }

    public void setCreatingUser(String creatingUser) {
        this.creatingUser = creatingUser;
    }

    public List<String> getCustomTargetsList() {
        return customTargetsList;
    }

    public void setCustomTargetsList(List<String> customTargetsList) {
        this.customTargetsList = customTargetsList;
    }

    public boolean isAllTargets() {
        return isAllTargets;
    }

    public void setAllTargets(boolean allTargets) {
        isAllTargets = allTargets;
    }

    public String getWhatIfTargetName() {
        return whatIfTargetName;
    }

    public void setWhatIfTargetName(String whatIfTargetName) {
        this.whatIfTargetName = whatIfTargetName;
    }

    public RelationTypeDTO getWhatIfTargetRelation() {
        return whatIfTargetRelation;
    }

    public void setWhatIfTargetRelation(RelationTypeDTO whatIfTargetRelation) {
        this.whatIfTargetRelation = whatIfTargetRelation;
    }

    public ConfigDTO getConfigDTO() {
        return configDTO;
    }

    public void setConfigDTO(ConfigDTO configDTO) {
        this.configDTO = configDTO;
    }

    public ExecutionTypeDTO getExecutionType() {
        return executionType;
    }

    public void setExecutionType(ExecutionTypeDTO executionType) {
        this.executionType = executionType;
    }


}
