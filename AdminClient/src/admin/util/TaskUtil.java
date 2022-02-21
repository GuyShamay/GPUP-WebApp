package admin.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dto.execution.ExecutionDTO;
import dto.util.DTOUtil;

import java.util.ArrayList;
import java.util.List;

public class TaskUtil {
    public static ExecutionDTO parseToExecutionDTO(JsonObject jsonObject) {
        ExecutionDTO executionDTO = new ExecutionDTO();
        if (jsonObject != null) {
            if (jsonObject.has("name")) {
                executionDTO.setName(jsonObject.get("name").getAsString());
            }
            if (jsonObject.has("creatingUser")) {
                executionDTO.setCreatingUser(jsonObject.get("creatingUser").getAsString());
            }
            if (jsonObject.has("status")) {
                executionDTO.setStatus(jsonObject.get("status").getAsString());
            }
            if (jsonObject.has("graphDTO")) {
                executionDTO.setGraphDTO(GraphUtil.parseToGraphDTO(jsonObject.get("graphDTO").getAsJsonObject()));
            }
            if (jsonObject.has("workersCount")) {
                executionDTO.setWorkersCount(jsonObject.get("workersCount").getAsInt());
            }
            if (jsonObject.has("price")) {
                executionDTO.setPrice(jsonObject.get("price").getAsInt());
            }
            if (jsonObject.has("type")) {
                executionDTO.setType(DTOUtil.ExecutionTypeDTO.valueOf(jsonObject.get("type").getAsString()));
            }
            if (jsonObject.has("progress")) {
                executionDTO.setProgress(jsonObject.get("progress").getAsInt());
            }
            if (jsonObject.has("workers")) {
                executionDTO.setWorkers(TaskUtil.parseToWorkersList(jsonObject.get("workers").getAsJsonArray()));
            }

        }
        return executionDTO;
    }

    private static List<String> parseToWorkersList(JsonArray jsonArray) {
        List<String> workers = new ArrayList<>();
        jsonArray.forEach(jsonElement -> workers.add(jsonElement.getAsString()));
        return workers;
    }


    public static class Selections {
        public static enum TargetSelect {
            all, custom, whatif, non
        }

        public static enum TypeSelect {
            simulation, compile, non, all
        }

    }
}
