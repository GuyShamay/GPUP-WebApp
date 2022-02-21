package dto.actions;

public class FindPathsConfigDTO {
    private String graphName;
    private String from;

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    private String to;
    private String relationType;


    public FindPathsConfigDTO(String graphName, String from, String to, String relationType) {
        this.graphName = graphName;
        this.from = from;
        this.to = to;
        this.relationType = relationType;
    }
    public FindPathsConfigDTO(){}
}
