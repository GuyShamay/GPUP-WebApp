package admin.component.taskcontrol;

import admin.util.Constants;
import admin.util.HttpClientUtil;
import dto.execution.LogsWithVersion;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Consumer;

import static admin.util.Constants.GSON_INST;

public class OutputAreaRefresher extends TimerTask {

    private final Consumer<LogsWithVersion> outputConsumer;
    private final IntegerProperty outputVersion;
    private final BooleanProperty shouldUpdate;
    private final String taskName;

    public OutputAreaRefresher(String taskName, IntegerProperty outputVersion, Consumer<LogsWithVersion> outputConsumer) {
        this.outputConsumer = outputConsumer;
        this.outputVersion = outputVersion;
        this.shouldUpdate = new SimpleBooleanProperty(true);
        this.taskName = taskName;
    }

    public void pause() {
        shouldUpdate.set(false);
    }

    public void resume() {
        shouldUpdate.set(true);
    }

    @Override
    public void run() {

        if (!shouldUpdate.get()) {
            return;
        }


        //noinspection ConstantConditions
        String finalUrl = HttpUrl
                .parse(Constants.EXECUTION_LOGS)
                .newBuilder()
                .addQueryParameter(Constants.VERSION, String.valueOf(outputVersion.get()))
                .addQueryParameter(Constants.TASK, taskName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String rawBody = response.body().string();
                    LogsWithVersion chatLinesWithVersion = GSON_INST.fromJson(rawBody, LogsWithVersion.class);
                    outputConsumer.accept(chatLinesWithVersion);
                } else {
                }
            }
        });

    }

}
