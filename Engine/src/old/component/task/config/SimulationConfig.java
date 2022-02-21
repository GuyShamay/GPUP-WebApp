package old.component.task.config;

import old.component.task.simulation.ProcessingTimeType;

public class SimulationConfig implements Config {

    private int processingTime;
    private ProcessingTimeType processingTimeType;
    private float successProb;
    private float successWithWarningsProb;

    public int getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    public ProcessingTimeType getProcessingTimeType() {
        return processingTimeType;
    }

    public void setProcessingTimeType(ProcessingTimeType processingTimeType) {
        this.processingTimeType = processingTimeType;
    }

    public float getSuccessProb() {
        return successProb;
    }

    public void setSuccessProb(int successProb) {
        this.successProb = (float) successProb / 100;
    }

    public float getSuccessWithWarningsProb() {
        return successWithWarningsProb;
    }

    public void setSuccessWithWarningsProb(int successWithWarningsProb) {
        this.successWithWarningsProb = (float) successWithWarningsProb / 100;
    }
}
