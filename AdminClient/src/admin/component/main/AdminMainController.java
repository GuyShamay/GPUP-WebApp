package admin.component.main;

import admin.component.dashboard.AdminDashboardController;
import admin.component.login.LoginController;
import admin.util.Constants;
import admin.util.HttpClientUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import okhttp3.HttpUrl;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static admin.util.Constants.ADMIN_DASHBOARD_FXML_RESOURCE_LOCATION;
import static admin.util.Constants.LOGIN_PAGE_FXML_RESOURCE_LOCATION;

public class AdminMainController implements Closeable {
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private Label usernameLabel;

    private StringProperty username;
    private Parent dashboardComponent;
    private AdminDashboardController adminDashboardController;
    private AnchorPane loginComponent;
    private LoginController loginController;
    private boolean isLoggedIn;
    // ------------------------------------------------------------------------------------------------------------
    // Controller:

    public AdminMainController() {
        username = new SimpleStringProperty();
    }

    @FXML
    public void initialize() {
        usernameLabel.textProperty().bind(Bindings.concat("Hello ", username, "!"));
        isLoggedIn = false;
        loadLoginPageAndSet();
        loadDashboard();
    }

    @Override
    public void close() throws IOException {
        adminDashboardController.close();
        if (isLoggedIn) {
            logout();
        }
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    public void updateUserName(String userName) {
        username.set(userName);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Login Component:
    private void logout() throws IOException {
        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT)
                .newBuilder()
                .addQueryParameter("username", username.get())
                .build()
                .toString();

        HttpClientUtil.runSync(finalUrl);
    }

    private void loadLoginPageAndSet() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            loginController = fxmlLoader.getController();
            loginController.setAdminMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToDashboard() {
        setMainPanelTo(dashboardComponent);
        adminDashboardController.setActive();
        isLoggedIn = true;

    }
    // ------------------------------------------------------------------------------------------------------------

    // Dashboard Component:

    private void loadDashboard() {
        URL dashboardUrl = getClass().getResource(ADMIN_DASHBOARD_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(dashboardUrl);
            dashboardComponent = fxmlLoader.load();
            adminDashboardController = fxmlLoader.getController();
            adminDashboardController.setAdminMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username.get();
    }
}
