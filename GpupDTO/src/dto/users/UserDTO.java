package dto.users;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserDTO {
    private final String username;
    private final String role;

    public UserDTO(String username, String role) {

        this.username=username;
        this.role=role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
