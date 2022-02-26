package admin.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.graph.GraphDTO;
import dto.target.FinishResultDTO;
import dto.target.RunResultDTO;
import dto.target.TargetDTO;
import dto.target.TargetTypeDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static admin.util.Constants.GRAPH_NAME;

public abstract class GraphUtil {

    public static void getGraphTargetsAsync(String graphName, Consumer<ObservableList<String>> consumer) {
        String finalUrl = HttpUrl.parse(Constants.GRAPH_TARGETS)
                .newBuilder()
                .addQueryParameter(GRAPH_NAME, graphName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    String targetsAsString = Objects.requireNonNull(response.body()).string();
                    JsonArray jsonArray = JsonParser.parseString(targetsAsString).getAsJsonArray();
                    ObservableList<String> targetNames = FXCollections.observableArrayList(parseJsonArrayToStringList(jsonArray));
                    Platform.runLater(() -> consumer.accept(targetNames));
                } else {
                    System.out.println("fail");
                }
            }
        });
    }


    public static List<String> parseJsonArrayToStringList(JsonArray jsonArray) {
        List<String> list = new ArrayList<>();
        jsonArray.forEach(jsonElement -> {
            list.add(jsonElement.getAsString());
        });
        return list;
    }

    public static List<TargetDTO> parseJsonArrayToTargetDTOList(JsonArray jsonArray) {
        List<TargetDTO> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            TargetDTO targetDTO = createTargetDTO(jsonObject);
            list.add(targetDTO);
        }
        return list;
    }

    private static TargetDTO createTargetDTO(JsonObject jsonObject) {
        TargetDTO targetDTO = new TargetDTO();

        targetDTO.setName(jsonObject.get("name").getAsString());
        targetDTO.setType(TargetTypeDTO.valueOf(jsonObject.get("type").getAsString()));
        targetDTO.setRunResult(RunResultDTO.valueOf(jsonObject.get("runResult").getAsString()));

        if (jsonObject.has("userData")) {
            targetDTO.setUserData(jsonObject.get("userData").getAsString());
        }
        if (jsonObject.has("finishResult")) {
            targetDTO.setFinishResult(FinishResultDTO.valueOf(jsonObject.get("finishResult").getAsString()));
        }
//        if (jsonObject.has("startRunningTime")) {
//            targetDTO.setStartRunningTime(Instant.parse(jsonObject.get("startRunningTime").getAsString()));
//        }
//        if (jsonObject.has("startWaitingTime")) {
//            targetDTO.setStartWaitingTime(Instant.parse(jsonObject.get("startWaitingTime").getAsString()));
//        }
//        if (jsonObject.has("taskRunDuration")) {
//            targetDTO.setTaskRunDuration(Duration.parse(jsonObject.get("taskRunDuration").getAsString()));
//        }
        targetDTO.setDependsOnList(getListIfExist(jsonObject, "dependsOnList"));
        targetDTO.setRequiredForList(getListIfExist(jsonObject, "requiredForList"));
        return targetDTO;
    }

    private static List<String> getListIfExist(JsonObject jsonObject, String memberName) {

        List<String> list = new ArrayList<>();
        if (jsonObject.has(memberName)) {
            JsonArray jsonArray = jsonObject.get(memberName).getAsJsonArray();
            jsonArray.forEach(jsonElement -> list.add(jsonElement.getAsString()));
        }
        return list;
    }

    public static GraphDTO parseToGraphDTO(JsonObject jsonObject) {
        GraphDTO graph = new GraphDTO();
        graph.setCreatingUser(jsonObject.get("creatingUser").getAsString());
        graph.setName(jsonObject.get("name").getAsString());
        graph.setIndependentCount(jsonObject.get("independentCount").getAsInt());
        graph.setLeafCount(jsonObject.get("leafCount").getAsInt());
        graph.setMiddleCount(jsonObject.get("middleCount").getAsInt());
        graph.setRootCount(jsonObject.get("rootCount").getAsInt());
        graph.setCompilationPrice(jsonObject.get("compilationPrice").getAsInt());
        graph.setSimulationPrice(jsonObject.get("simulationPrice").getAsInt());

        if (jsonObject.has("targetsList")) {
            List<TargetDTO> list = new ArrayList<>();
            JsonArray jsonArray = jsonObject.get("targetsList").getAsJsonArray();
            jsonArray.forEach(targetAsJsonElem -> {
                list.add(createTargetDTO(targetAsJsonElem.getAsJsonObject()));
            });
            graph.setTargetsList(list);
        }
        graph.getTargetsList();
        return graph;
    }
}
