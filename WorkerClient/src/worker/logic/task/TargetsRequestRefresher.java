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
import worker.logic.Worker;
import worker.logic.target.TaskTarget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static worker.client.util.Constants.*;

public class TargetsRequestRefresher extends TimerTask {
    private final Consumer<List<NewExecutionTargetDTO>> targetsConsumer;
    private final BooleanProperty shouldUpdate;
    private final Worker worker;

    public TargetsRequestRefresher(Worker worker, Consumer<List<NewExecutionTargetDTO>> targetsConsumer) {
        this.targetsConsumer = targetsConsumer;
        shouldUpdate = new SimpleBooleanProperty(true);
        this.worker = worker;
    }

    @Override
    public void run() {
        if (worker.isRegisterAny()) {
            synchronized (worker) {
                worker.getExecutionsMap().forEach((taskName, execution) -> {
                    if (execution.getExecutionStatus().equals(WorkerExecutionStatus.Active)) {
                        String finalUrl = HttpUrl
                                .parse(Constants.TARGET_REQUEST)
                                .newBuilder()
                                .addQueryParameter(TASK_NAME, taskName)
                                .build()
                                .toString();

                        HttpClientUtil.runAsync(finalUrl, new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                //  Platform.runLater(() -> errorConsumer.accept("Error: failed request"));
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                String responseBody = response.body().string();
                                switch (response.code()) {
                                    case SC_EXEC_ON:
                                        if (responseBody != null && !responseBody.isEmpty()) {
                                            JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                                            List<NewExecutionTargetDTO> list = parseTargetsList(jsonArray);
                                            targetsConsumer.accept(list);
                                        }
                                        break;
                                    case SC_EXEC_STOPPED:
                                        worker.updateWorkerExecutionStatus(taskName, WorkerExecutionStatus.StoppedByAdmin);
                                        break;
                                    case SC_EXEC_DONE:
                                        worker.updateWorkerExecutionStatus(taskName, WorkerExecutionStatus.Finished);

                                        break;
                                }
                            }
                        });
                    }
                });
            }
        }
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
