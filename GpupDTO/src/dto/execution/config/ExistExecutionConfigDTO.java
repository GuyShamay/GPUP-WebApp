package dto.execution.config;

public class ExistExecutionConfigDTO {
    private String baseName;
    private boolean isIncremental;

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean incremental) {
        isIncremental = incremental;
    }
}
