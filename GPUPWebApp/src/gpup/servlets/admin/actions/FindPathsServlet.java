package gpup.servlets.admin.actions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.actions.FindPathsConfigDTO;
import engine.NewEngine;
import engine.target.TargetsRelationType;
import gpup.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static gpup.constants.Constants.*;

@WebServlet("/admin/find-paths")
public class FindPathsServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json");
        Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
        String reqBodyAsString = s.hasNext() ? s.next() : null;
        FindPathsConfigDTO findPathsConfigDTO = parseBodyToFindPathsConfigDTO(reqBodyAsString);
        getPaths(response, findPathsConfigDTO);
    }

    private void getPaths(HttpServletResponse response, FindPathsConfigDTO findPathsDTO) throws IOException {
        boolean isReqBodyValid = validateFindPathsDTO(response, findPathsDTO);
        if (isReqBodyValid) {
            NewEngine engine = ServletUtils.getEngine(getServletContext());
            // get paths from engine
            boolean targetsExist;
            synchronized (this) {
                targetsExist = engine.isTargetExistInGraph(findPathsDTO.getGraphName(), findPathsDTO.getFrom())
                        && engine.isTargetExistInGraph(findPathsDTO.getGraphName(), findPathsDTO.getTo());
            }
            if (targetsExist) {
                TargetsRelationType relationType = parseRelationType(findPathsDTO.getRelationType());
                List<String> paths = engine.getPathsDTO(findPathsDTO.getGraphName(), findPathsDTO.getFrom(), findPathsDTO.getTo(), relationType);
                try (PrintWriter out = response.getWriter()) {
                    String json = GSON_INST.toJson(paths);
                    System.out.println(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.println(json);
                    out.flush();
                }
            } else {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                try (PrintWriter out = response.getWriter()) {
                    out.println("Wrong path's data sent to the system");
                    out.flush();
                }
            }

        }
    }

    private TargetsRelationType parseRelationType(String relationType) {
        return Objects.equals(relationType, "DependsOn") ? TargetsRelationType.DependsOn : TargetsRelationType.RequiredFor;
    }

    private boolean validateFindPathsDTO(HttpServletResponse response, FindPathsConfigDTO findPathsDTO) throws IOException {
        boolean res = false;
        if (findPathsDTO != null) {
            if (findPathsDTO.getGraphName() == null || findPathsDTO.getGraphName().isEmpty()) {
                updateResponse(response, "No graph was sent to server", HttpServletResponse.SC_PARTIAL_CONTENT);
            } else {
                if (findPathsDTO.getFrom() == null || findPathsDTO.getFrom().isEmpty()
                        || findPathsDTO.getTo() == null || findPathsDTO.getTo().isEmpty()
                        || findPathsDTO.getRelationType() == null || findPathsDTO.getRelationType().isEmpty()) {
                    updateResponse(response, "No targets were sent to server", HttpServletResponse.SC_PARTIAL_CONTENT);
                }
                if (findPathsDTO.getFrom().equals(findPathsDTO.getTo())) {
                    updateResponse(response, "The same target was selected", HttpServletResponse.SC_PARTIAL_CONTENT);
                } else {
                    res = true;
                }
            }
            return res;
        } else {
            return false;
        }
    }

    private void updateResponse(HttpServletResponse response, String msg, int code) throws IOException {
        String errorMsg = "";
        response.setStatus(code);
        response.getOutputStream().print(msg);
        response.getOutputStream().flush();
    }

    private FindPathsConfigDTO parseBodyToFindPathsConfigDTO(String reqBodyAsString) {
        if (reqBodyAsString != null) {
            JsonObject jsonObject = JsonParser.parseString(reqBodyAsString).getAsJsonObject();
            FindPathsConfigDTO findPathsDTO = new FindPathsConfigDTO();
            findPathsDTO.setGraphName(jsonObject.get("graphName").getAsString());
            findPathsDTO.setFrom(jsonObject.get("from").getAsString());
            findPathsDTO.setTo(jsonObject.get("to").getAsString());
            findPathsDTO.setRelationType(jsonObject.get("relationType").getAsString());
            return findPathsDTO;
        }
        return null;
    }
}