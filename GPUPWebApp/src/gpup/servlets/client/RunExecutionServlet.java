package gpup.servlets.client;

import dto.execution.RunExecutionDTO;
import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static gpup.constants.Constants.*;


@WebServlet(name = "RunExecutionServlet", urlPatterns = {"/run-execution"})
public class RunExecutionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        String taskNameFromParameter = request.getParameter(TASK);
        if (taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
            String errorMessage = "Conflict in Server state";
            response.getOutputStream().print(errorMessage);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            synchronized (this) { // maybe not needed?????
                if (engine.isExecutionExist(taskNameFromParameter)) {
                    try (PrintWriter out = response.getWriter()) {
                        RunExecutionDTO runExecutionDTO = engine.getRunExecutionDTO(taskNameFromParameter);
                        String runExecAsJson = GSON_INST.toJson(runExecutionDTO);
                        out.println(runExecAsJson);
                        out.flush();
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                } else {
                    String errorMsg = "No Task named " + taskNameFromParameter + " in the system";
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getOutputStream().print(errorMsg);
                }
            }
        }
    }

}