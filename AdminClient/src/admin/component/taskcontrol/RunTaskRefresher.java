package admin.component.taskcontrol;

import admin.util.Constants;
import admin.util.HttpClientUtil;
import admin.util.TaskUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.execution.RunExecutionDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Consumer;

import static admin.util.Constants.TASK;

public class RunTaskRefresher extends TimerTask {
    private final Consumer<RunExecutionDTO> taskConsumer;
    private final Consumer<String> errorConsumer;
    private final BooleanProperty shouldUpdate;
    private final String taskName;

    public RunTaskRefresher(String taskName, BooleanProperty shouldUpdate, Consumer<RunExecutionDTO> taskConsumer, Consumer<String> errorConsumer) {
        this.shouldUpdate = shouldUpdate;
        this.taskName = taskName;
        this.taskConsumer = taskConsumer;
        this.errorConsumer = errorConsumer;
    }

    public void setShouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate.set(shouldUpdate);
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }

        String finalUrl = HttpUrl
                .parse(Constants.RUN_EXECUTION)
                .newBuilder()
                .addQueryParameter(TASK, taskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorConsumer.accept("Error: failed request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String taskAsString = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(taskAsString).getAsJsonObject();
                    RunExecutionDTO runExecutionDTO = TaskUtil.parseToRunExecutionDTO(jsonObject); // need to update
                   Platform.runLater(()->{
                       taskConsumer.accept(runExecutionDTO); // need to update
                   });
                }
            }
        });
    }
}

