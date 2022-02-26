package worker.logic.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dto.execution.LightWorkerExecution;
import dto.target.NewExecutionTargetDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;
import worker.client.util.TaskUtil;
import worker.logic.Worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class LightWorkerExecutionRefresher extends TimerTask {
    private final Consumer<List<LightWorkerExecution>> lightWorkerExecutionsConsumer;
    private final Worker worker;
    private final BooleanProperty shouldUpdate;

    public LightWorkerExecutionRefresher(Consumer<List<LightWorkerExecution>> lightWorkerExecutionsConsumer) {
        this.shouldUpdate = new SimpleBooleanProperty(false);
        this.lightWorkerExecutionsConsumer = lightWorkerExecutionsConsumer;
        this.worker = null;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }
        HttpClientUtil.runAsync(Constants.LIGHT_W_EXEC, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (responseBody != null && !responseBody.isEmpty()) {
                    JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                    if (jsonArray == null || jsonArray.isEmpty()) {
                        lightWorkerExecutionsConsumer.accept(null);
                    } else {
                        List<LightWorkerExecution> list = parseToLightList(jsonArray);
                        lightWorkerExecutionsConsumer.accept(list);
                    }
                }
            }
        });
    }

    private List<LightWorkerExecution> parseToLightList(JsonArray jsonArray) {
        List<LightWorkerExecution> list = new ArrayList<>();
        jsonArray.forEach(jsonElement -> list.add(TaskUtil.parseToLightWorkerExecution(jsonElement.getAsJsonObject())));
        return list;
    }

    public void pause() {
        shouldUpdate.set(false);
    }

    public void resume() {
        shouldUpdate.set(true);
    }
}

