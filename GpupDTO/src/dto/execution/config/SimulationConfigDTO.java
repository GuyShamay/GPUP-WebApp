package dto.execution.config;

import dto.util.DTOUtil;

public class SimulationConfigDTO implements ConfigDTO {
    private int processingTime;
    private boolean isRandom;
    private float successProb;
    private float successWithWarningsProb;

    public int getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    public boolean getIsRandom() {
        return isRandom;
    }

    public void setIsRandom(boolean isRandom) {
        this.isRandom = isRandom;
    }

    public float getSuccessProb() {
        return successProb;
    }

    public void setSuccessProb(float successProb) {
        this.successProb = successProb;
    }

    public float getSuccessWithWarningsProb() {
        return successWithWarningsProb;
    }

    public void setSuccessWithWarningsProb(float successWithWarningsProb) {
        this.successWithWarningsProb = successWithWarningsProb;
    }
}
