package old.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CircuitDTO {
    public List<String> getCircuit() {
        if (circuit != null) {
            return circuit;
        }
        throw new NoSuchElementException();
    }

    private final List<String> circuit;

    @Override
    public String toString() {
        String str = "";

        if (circuit != null) {
            for (String s : circuit) {
                str += s + " --> ";
            }
            str = str.substring(0, str.length() - 4);
        } else
            str = null;

        return str;
    }

    public CircuitDTO(List<String> circuit) {
        this.circuit = circuit;
    }

}
