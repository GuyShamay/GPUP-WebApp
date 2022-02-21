package old.dto;

import engine.target.TargetsRelationType;

import java.util.List;
import java.util.NoSuchElementException;

public class PathsDTO {
    public List<String> getPaths() {
        if (paths != null && paths.size() !=0) {
            return paths;
        }
        throw new NoSuchElementException();
    }

    public TargetsRelationType getType() {
        return type;
    }

    private final List<String> paths;
    private final String srcName;
    private final String destName;
    private final TargetsRelationType type;

    public PathsDTO(List<String> paths, String srcName, String destName, TargetsRelationType type) {
        this.paths = paths;
        this.srcName = srcName;
        this.destName = destName;
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(type.name()).append(" Paths from ").append(srcName).append(" to ").append(destName).append(":\n");
        if (paths == null || paths.size() == 0) {
            str.append("There are no paths.");
        } else {
            paths.forEach(s -> str.append(s).append("\n"));
        }
        return str.toString();
    }
}
