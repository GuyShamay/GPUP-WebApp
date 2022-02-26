package worker.client.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dto.execution.ExecutionDTO;
import dto.execution.LightWorkerExecution;
import dto.execution.config.CompilationConfigDTO;
import dto.execution.config.ConfigDTO;
import dto.execution.config.SimulationConfigDTO;
import dto.graph.GraphDTO;
import dto.target.*;
import dto.util.DTOUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import worker.logic.task.ExecutionType;
import worker.logic.task.WorkerExecution;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public abstract class TaskUtil {
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
                executionDTO.setGraphDTO(parseToGraphDTO(jsonObject.get("graphDTO").getAsJsonObject()));
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
            if (jsonObject.has("config")) {
                executionDTO.setConfig(parseExecutionDetails(jsonObject.get("config").getAsJsonObject(), executionDTO.getType()));
            }
        }
        return executionDTO;
    }

    private static ConfigDTO parseExecutionDetails(JsonObject config, DTOUtil.ExecutionTypeDTO type) {
        if (type.equals(DTOUtil.ExecutionTypeDTO.Compilation)) {
            CompilationConfigDTO details = new CompilationConfigDTO();
            if (config.has("destDir")) {
                details.setDestDir(config.get("destDir").getAsString());
            }
            if (config.has("srcDir")) {
                details.setSrcDir(config.get("srcDir").getAsString());
            }
            return details;

        } else if (type.equals(DTOUtil.ExecutionTypeDTO.Simulation)) {
            SimulationConfigDTO details = new SimulationConfigDTO();
            if (config.has("isRandom")) {
                details.setIsRandom(config.get("isRandom").getAsBoolean());
            }
            if (config.has("processingTime")) {
                details.setProcessingTime(config.get("processingTime").getAsInt());
            }
            if (config.has("successProb")) {
                details.setSuccessProb(config.get("successProb").getAsFloat());
            }
            if (config.has("successWithWarningsProb")) {
                details.setSuccessWithWarningsProb(config.get("successWithWarningsProb").getAsFloat());
            }
            return details;
        }
        return null;
    }

    private static List<String> parseToWorkersList(JsonArray jsonArray) {
        List<String> workers = new ArrayList<>();
        jsonArray.forEach(jsonElement -> workers.add(jsonElement.getAsString()));
        return workers;
    }

    private static GraphDTO parseToGraphDTO(JsonObject jsonObject) {
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
        if (jsonObject.has("startRunningTime")) {
            targetDTO.setStartRunningTime(Instant.parse(jsonObject.get("startRunningTime").getAsString()));
        }
        if (jsonObject.has("startWaitingTime")) {
            targetDTO.setStartWaitingTime(Instant.parse(jsonObject.get("startWaitingTime").getAsString()));
        }
        if (jsonObject.has("taskRunDuration")) {
            targetDTO.setTaskRunDuration(Duration.parse(jsonObject.get("taskRunDuration").getAsString()));
        }
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

    public static WorkerExecution parseToWorkerExecution(JsonObject jsonObject) {
        WorkerExecution execution = new WorkerExecution();
        if (jsonObject.has("name")) {
            execution.setName(jsonObject.get("name").getAsString());
        }
        if (jsonObject.has("workersCount")) {
            execution.setWorkersCount(jsonObject.get("workersCount").getAsInt());
        }
        if (jsonObject.has("progress")) {
            execution.setProgress(jsonObject.get("progress").getAsDouble());
        }
        if (jsonObject.has("price")) {
            execution.setPrice(jsonObject.get("price").getAsInt());
        }
        if (jsonObject.has("type")) {
            execution.setType(ExecutionType.valueOf(jsonObject.get("type").getAsString()));
        }
        if (jsonObject.has("executionDetails")) {
            execution.setExecutionDetails(parseExecutionDetails(jsonObject.get("executionDetails").getAsJsonObject(), DTOUtil.ExecutionTypeDTO.valueOf(execution.getType().name())));
        }
        return execution;
    }

    public static NewExecutionTargetDTO parseToNewExecutionTargetDTO(JsonObject jsonObject) {
        if (jsonObject != null) {
            NewExecutionTargetDTO newTarget = new NewExecutionTargetDTO();
            if (jsonObject.has("userData")) {
                newTarget.setUserData(jsonObject.get("userData").getAsString());
            }
            if (jsonObject.has("name")) {
                newTarget.setName(jsonObject.get("name").getAsString());
            }
            if (jsonObject.has("executionName")) {
                newTarget.setExecutionName(jsonObject.get("executionName").getAsString());
            }
            return newTarget;
        }
        return null;
    }

    public static boolean confirmationAlert(String header, String msg) {
        final boolean[] result = new boolean[1];
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setTitle(header);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                result[0] = true;
            }
        });
        return result[0];
    }

    public static LightWorkerExecution parseToLightWorkerExecution(JsonObject jsonObject) {

        LightWorkerExecution lightExec = new LightWorkerExecution();
        if (jsonObject.has("name")) {
            lightExec.setName(jsonObject.get("name").getAsString());
        }
        if (jsonObject.has("workers")) {
            lightExec.setWorkers(jsonObject.get("workers").getAsInt());
        }
        if (jsonObject.has("progress")) {
            lightExec.setProgress(jsonObject.get("progress").getAsDouble());
        }
        return lightExec;
    }
}