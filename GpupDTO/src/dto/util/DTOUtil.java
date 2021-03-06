package dto.util;

public abstract class DTOUtil {


    public static enum ExecutionTypeDTO {
        Simulation,
        Compilation
    }

    public static enum RelationTypeDTO {
        DependsOn,
        RequiredFor
    }

    public static enum ExecutionControlDTO{
        Pause,
        Resume,
        Stop
    }
}
