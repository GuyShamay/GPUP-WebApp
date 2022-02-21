package old.dto;

public interface GPUPConsumer {
    default void setTaskOutput(TaskOutputDTO taskOutput) {};

    default String getName() {
        return "Name";
    }
}
