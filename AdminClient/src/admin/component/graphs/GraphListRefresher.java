package admin.component.graphs;

import admin.util.Constants;
import admin.util.GraphUtil;
import admin.util.HttpClientUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.graph.GraphDTO;
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

public class GraphListRefresher extends TimerTask {

    private final Consumer<List<GraphDTO>> graphsListConsumer;
    private final Consumer<String> errorConsumer;

    public void setShouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate.set(shouldUpdate);
    }

    private final BooleanProperty shouldUpdate;


    public GraphListRefresher(BooleanProperty shouldUpdate, Consumer<List<GraphDTO>> graphListConsumer, Consumer<String> errorConsumer) {
        this.shouldUpdate = shouldUpdate;
        this.graphsListConsumer = graphListConsumer;
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }

        HttpClientUtil.runAsync(Constants.GRAPHS_LIST, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorConsumer.accept("Error: failed request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String graphsListAsString = response.body().string();
                JsonArray jsonArray = JsonParser.parseString(graphsListAsString).getAsJsonArray();
                graphsListConsumer.accept(parseGraphsList(jsonArray));
            }
        });
    }

    private List<GraphDTO> parseGraphsList(JsonArray jsonArray) {
        List<GraphDTO> list = null;
        if (jsonArray != null) {
            list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                GraphDTO graph = GraphUtil.parseToGraphDTO(jsonObject);
                list.add(graph);
            }
        }
        return list;
    }
}
