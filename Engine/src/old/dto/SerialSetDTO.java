package old.dto;

import engine.serialset.SerialSet;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class SerialSetDTO {

    private final SimpleIntegerProperty count;
    private final SimpleStringProperty name;
    private final SimpleStringProperty targetsAsString;

    public SerialSetDTO(SerialSet serialSet) {
        name = new SimpleStringProperty(serialSet.getName());
        count = new SimpleIntegerProperty(serialSet.getTargets().size());
        targetsAsString = new SimpleStringProperty(serialSet.getTargetAsString());
        targets = new ArrayList<>();
        serialSet.getTargets().forEach(target -> targets.add(new TargetInfoDTO(target)));
    }

    public int getCount() {
        return count.get();
    }

    public SimpleIntegerProperty countProperty() {
        return count;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getTargetsAsString() {
        return targetsAsString.get();
    }

    public SimpleStringProperty targetsAsStringProperty() {
        return targetsAsString;
    }

    public List<TargetInfoDTO> getTargets() {
        return targets;
    }

    private List<TargetInfoDTO> targets;
}
