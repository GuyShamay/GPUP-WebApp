package worker.client.component.login;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import worker.client.component.main.WorkerMainController;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;

import java.io.IOException;

public class LoginController {
    @FXML
    public TextField userNameTextField;
    @FXML
    public Label errorMessageLabel;
    @FXML
    private Spinner<Integer> threadsSpinner;

    private WorkerMainController workerMainController;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
        userNameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login();
            }
        });
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5);
        threadsSpinner.setValueFactory(factory);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {
        login();
    }

    private void login() {
        String userName = userNameTextField.getText();
        Integer threadsCount = threadsSpinner.getValue();
        if (userName.isEmpty()) {
            errorMessageProperty.set("Username is empty, please try again.");
            return;
        }
        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .addQueryParameter("threads", threadsCount.toString())
                .build()
                .toString();

        //updateHttpStatusLine("New request is launched for: " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Fail Login to server")
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() -> errorMessageProperty.set("Failure: " + responseBody));
                } else {
                    Platform.runLater(() -> {
                        workerMainController.setUsername(userName);
                        workerMainController.setWorkerThreads(threadsCount);
                        workerMainController.switchToDashboardFirstTime();
                    });
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

//    private void updateHttpStatusLine(String data) {
//        adminMainController.updateHttpLine(data);
//    }

    public void setWorkerMainController(WorkerMainController workerMainController) {
        this.workerMainController = workerMainController;
    }
}
