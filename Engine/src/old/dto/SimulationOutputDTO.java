package old.dto;

public class SimulationOutputDTO implements TaskOutputDTO {
    private final long sleepingTime;

    public SimulationOutputDTO(long sleepingTime) {
        this.sleepingTime = sleepingTime;
    }

    @Override
    public String toString() {
        return "The predicted sleeping time is " + sleepingTime + " ms" +
                "\nThe target is going to sleep" +
                "\nThe Target just woke up\n";
    }
}
