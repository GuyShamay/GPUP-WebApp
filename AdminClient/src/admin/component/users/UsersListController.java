package admin.component.users;

import dto.users.UserDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static admin.util.Constants.REFRESH_RATE;

public class UsersListController implements Closeable {

    @FXML
    private TableColumn<UserDTO, String> usernameCol;
    @FXML
    private TableColumn<UserDTO, String> userRoleCol;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TableView<UserDTO> usersTable;

    private Timer timer;
    private TimerTask listRefresher;
    private final BooleanProperty autoUpdate;
    private final IntegerProperty totalUsers;

    public UsersListController() {
        autoUpdate = new SimpleBooleanProperty(true);
        totalUsers = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(Bindings.concat("Total Users: ", totalUsers.asString()));
        usernameCol.setCellValueFactory(new PropertyValueFactory<UserDTO, String>("username"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<UserDTO, String>("role"));
    }

    public BooleanProperty autoUpdatesProperty() {
        return autoUpdate;
    }

    private void updateUsersList(List<UserDTO> usersNames) {
        Platform.runLater(() -> {
            ObservableList<UserDTO> items = usersTable.getItems();
            items.clear();
            items.addAll(usersNames);
            totalUsers.set(usersNames.size());
        });
    }

    public void startListRefresher() {
        listRefresher = new UsersListRefresher(autoUpdate, this::updateUsersList);
        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }


    @Override
    public void close() {
        usersTable.getItems().clear();
        totalUsers.set(0);
        if (listRefresher != null && timer != null) {
            listRefresher.cancel();
            timer.cancel();
        }
    }
}
