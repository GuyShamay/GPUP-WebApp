package worker.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import worker.client.component.main.WorkerMainController;
import worker.client.util.HttpClientUtil;
import worker.logic.Worker;

import java.io.IOException;
import java.net.URL;

import static worker.client.util.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class WorkerClient extends Application {

    private WorkerMainController workerMainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("Worker App");

        URL loginPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();
            workerMainController = fxmlLoader.getController();
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
        workerMainController.close();
    }
}