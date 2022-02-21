package admin.component.tasks;

import admin.util.Constants;
import admin.util.HttpClientUtil;
import admin.util.TaskUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.execution.ExecutionDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TasksListRefresher extends TimerTask {
    private final Consumer<List<ExecutionDTO>> tasksListConsumer;
    private final Consumer<String> errorConsumer;

    public void setShouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate.set(shouldUpdate);
    }

    private final BooleanProperty shouldUpdate;


    public TasksListRefresher(BooleanProperty shouldUpdate, Consumer<List<ExecutionDTO>> tasksListConsumer, Consumer<String> errorConsumer) {
        this.shouldUpdate = shouldUpdate;
        this.tasksListConsumer = tasksListConsumer;
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }

        HttpClientUtil.runAsync(Constants.TASKS_LIST, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorConsumer.accept("Error: failed request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String tasksListAsString = response.body().string();
                JsonArray jsonArray = JsonParser.parseString(tasksListAsString).getAsJsonArray();
                tasksListConsumer.accept(parseTasksList(jsonArray));
            }
        });
    }

    private List<ExecutionDTO> parseTasksList(JsonArray jsonArray) {
        List<ExecutionDTO> list = null;
        if (jsonArray != null) {
            list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                ExecutionDTO task = TaskUtil.parseToExecutionDTO(jsonObject);
                list.add(task);
            }
        }
        return list;
    }
}

