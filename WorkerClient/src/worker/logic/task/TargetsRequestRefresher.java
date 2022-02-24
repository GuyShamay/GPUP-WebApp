package worker.logic.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.target.NewExecutionTargetDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.client.util.TaskUtil;
import worker.logic.target.TaskTarget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static worker.client.util.Constants.GSON_INST;
import static worker.client.util.Constants.TASK_NAME;

public class TargetsRequestRefresher extends TimerTask {
    private final Consumer<List<NewExecutionTargetDTO>> targetsConsumer;
    private final Consumer<String> errorConsumer;
    private final BooleanProperty shouldUpdate;
    private String taskName;

    public TargetsRequestRefresher(String taskName, Consumer<List<NewExecutionTargetDTO>> targetsConsumer, Consumer<String> errorConsumer) {
        this.targetsConsumer = targetsConsumer;
        this.errorConsumer = errorConsumer;
        shouldUpdate = new SimpleBooleanProperty(true);
        this.taskName = taskName;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }

        String finalUrl = HttpUrl
                .parse(Constants.TARGET_REQUEST)
                .newBuilder()
                .addQueryParameter(TASK_NAME, taskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorConsumer.accept("Error: failed request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                List<NewExecutionTargetDTO> list = parseTargetsList(jsonArray);
                targetsConsumer.accept(list);
            }
        });
    }

    private List<NewExecutionTargetDTO> parseTargetsList(JsonArray jsonArray) {
        List<NewExecutionTargetDTO> list = null;
        if (jsonArray != null) {
            list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                list.add(TaskUtil.parseToNewExecutionTargetDTO(jsonObject));
            }
        }
        return list;
    }

    public void pause() {
        shouldUpdate.set(false);
    }

    public void resume() {
        shouldUpdate.set(true);
    }
}
