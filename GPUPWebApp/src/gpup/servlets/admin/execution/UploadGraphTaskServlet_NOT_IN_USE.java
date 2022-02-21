package gpup.servlets.admin.execution;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.execution.config.CompilationConfigDTO;
import dto.execution.config.ExecutionConfigDTO;
import dto.execution.config.SimulationConfigDTO;
import dto.util.DTOUtil;
import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@WebServlet(name = "UploadGraphTaskServlet", urlPatterns = {"/admin/upload-graph-task"})
public class UploadGraphTaskServlet_NOT_IN_USE extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
        String reqBodyAsString = s.hasNext() ? s.next() : null;
        ExecutionConfigDTO executionConfigDTO = parseBodyToExecutionConfigDTO(reqBodyAsString);
        if (executionConfigDTO != null) {
            NewEngine engine = ServletUtils.getEngine(getServletContext());
            try (PrintWriter out = response.getWriter()) {
                try {
                    engine.addExecution(executionConfigDTO);
                    response.setStatus(HttpServletResponse.SC_OK);
                    String msg = "Task uploaded successfully!";
                    out.println(msg);
                    out.flush();
                } catch (RuntimeException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.println(e.getMessage());
                    out.flush();
                }
            }
        }
    }

    private ExecutionConfigDTO parseBodyToExecutionConfigDTO(String reqBodyAsString) {
        if (reqBodyAsString != null || !reqBodyAsString.isEmpty()) {
            JsonObject jsonObject = JsonParser.parseString(reqBodyAsString).getAsJsonObject();
            ExecutionConfigDTO executionConfig = new ExecutionConfigDTO();

            if (jsonObject.has("name")) {
                executionConfig.setName(jsonObject.get("name").getAsString());
            }
            if (jsonObject.has("graphName")) {
                executionConfig.setGraphName(jsonObject.get("graphName").getAsString());
            }
            if (jsonObject.has("creatingUser")) {
                executionConfig.setCreatingUser(jsonObject.get("creatingUser").getAsString());
            }
            if (jsonObject.has("whatIfTargetName")) {
                executionConfig.setWhatIfTargetName(jsonObject.get("whatIfTargetName").getAsString());
            }
            if (jsonObject.has("isAllTargets")) {
                executionConfig.setAllTargets(jsonObject.get("isAllTargets").getAsBoolean());
            }
            if (jsonObject.has("isCustomTargets")) {
                executionConfig.setCustomTargets(jsonObject.get("isCustomTargets").getAsBoolean());
            }
            if (jsonObject.has("isWhatIfTarget")) {
                executionConfig.setWhatIfTarget(jsonObject.get("isWhatIfTarget").getAsBoolean());
            }
            if (jsonObject.has("whatIfTargetRelation")) {
                executionConfig.setWhatIfTargetRelation(DTOUtil.RelationTypeDTO.valueOf(jsonObject.get("whatIfTargetRelation").getAsString()));
            }
            if (jsonObject.has("whatIfTargetRelation")) {
                executionConfig.setWhatIfTargetRelation(DTOUtil.RelationTypeDTO.valueOf(jsonObject.get("whatIfTargetRelation").getAsString()));
            }
            if (jsonObject.has("executionType")) {
                executionConfig.setExecutionType(DTOUtil.ExecutionTypeDTO.valueOf(jsonObject.get("executionType").getAsString()));
            }
            if (jsonObject.has("price")) {
                executionConfig.setPrice(jsonObject.get("price").getAsInt());
            }
            setConfigDTO(jsonObject, executionConfig);
            setCustomList(jsonObject, executionConfig);

            return executionConfig;
        }
        return null;
    }

    private void setCustomList(JsonObject jsonObject, ExecutionConfigDTO executionConfig) {
        if (jsonObject.has("customTargetsList")) {
            List<String> targetList = new ArrayList<>();
            JsonArray jsonArray = jsonObject.get("customTargetsList").getAsJsonArray();
            jsonArray.forEach(jsonElement -> {
                targetList.add(jsonElement.getAsString());
            });
            executionConfig.setCustomTargetsList(targetList);
        }
    }

    private void setConfigDTO(JsonObject jsonObject, ExecutionConfigDTO executionConfig) {
        if (jsonObject.has("configDTO")) {
            JsonObject config = jsonObject.get("configDTO").getAsJsonObject();

            if (executionConfig.getExecutionType().equals(DTOUtil.ExecutionTypeDTO.Compilation)) {
                CompilationConfigDTO configDTO = new CompilationConfigDTO();
                if (config.has("destDir")) {
                    configDTO.setDestDir(config.get("destDir").getAsString());
                }
                if (config.has("srcDir")) {
                    configDTO.setDestDir(config.get("srcDir").getAsString());
                }
                executionConfig.setConfigDTO(configDTO);
            } else if (executionConfig.getExecutionType().equals(DTOUtil.ExecutionTypeDTO.Simulation)) {
                SimulationConfigDTO configDTO = new SimulationConfigDTO();
                if (config.has("processingTime")) {
                    configDTO.setProcessingTime(config.get("processingTime").getAsInt());
                }
                if (config.has("isRandom")) {
                    configDTO.setIsRandom(config.get("isRandom").getAsBoolean());
                }
                if (config.has("successProb")) {
                    configDTO.setSuccessProb(config.get("successProb").getAsFloat());
                }
                if (config.has("successWithWarningsProb")) {
                    configDTO.setSuccessWithWarningsProb(config.get("successWithWarningsProb").getAsFloat());
                }
                executionConfig.setConfigDTO(configDTO);
            }
        }
    }
}
