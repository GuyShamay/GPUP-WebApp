package admin.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dto.execution.ExecutionDTO;
import dto.execution.RunExecutionDTO;
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
                executionDTO.setProgress(jsonObject.get("progress").getAsDouble());
            }
            if (jsonObject.has("workers")) {
                executionDTO.setWorkers(TaskUtil.parseToList(jsonObject.get("workers").getAsJsonArray()));
            }

        }
        return executionDTO;
    }

    private static List<String> parseToList(JsonArray jsonArray) {
        List<String> list = new ArrayList<>();
        jsonArray.forEach(jsonElement -> list.add(jsonElement.getAsString()));
        return list;
    }

    public static RunExecutionDTO parseToRunExecutionDTO(JsonObject jsonObject) {
        RunExecutionDTO runExecution = new RunExecutionDTO();
        if (jsonObject != null) {
            if (jsonObject.has("status")) {
                runExecution.setStatus(jsonObject.get("status").getAsString());
            }
            if (jsonObject.has("progress")) {
                runExecution.setProgress(jsonObject.get("progress").getAsDouble());
            }
            if (jsonObject.has("frozen")) {
                runExecution.setFrozen(parseToList(jsonObject.get("frozen").getAsJsonArray()));
            }
            if (jsonObject.has("waiting")) {
                runExecution.setWaiting(parseToList(jsonObject.get("waiting").getAsJsonArray()));
            }
            if (jsonObject.has("inProcess")) {
                runExecution.setInProcess(parseToList(jsonObject.get("inProcess").getAsJsonArray()));
            }
            if (jsonObject.has("skipped")) {
                runExecution.setSkipped(parseToList(jsonObject.get("skipped").getAsJsonArray()));
            }
            if (jsonObject.has("failure")) {
                runExecution.setFailure(parseToList(jsonObject.get("failure").getAsJsonArray()));
            }
            if (jsonObject.has("success")) {
                runExecution.setSuccess(parseToList(jsonObject.get("success").getAsJsonArray()));
            }
            if (jsonObject.has("warnings")) {
                runExecution.setWarnings(parseToList(jsonObject.get("warnings").getAsJsonArray()));
            }
            return runExecution;
        }
        return null;
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
