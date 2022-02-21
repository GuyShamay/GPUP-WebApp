package admin;

import admin.component.main.AdminMainController;
import admin.util.HttpClientUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static admin.util.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class AdminClient extends Application {

    private AdminMainController adminMainController;
public static Stage mainStage;
    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("Admin App");

        URL loginPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();
            adminMainController = fxmlLoader.getController();

            Scene scene = new Scene(root, 1000, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        adminMainController.close();
    }
}
