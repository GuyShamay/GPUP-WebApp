package gpup.servlets.worker.execution;

import dto.target.NewExecutionTargetDTO;
import engine.NewEngine;
import gpup.util.ServletUtils;
import gpup.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static gpup.constants.Constants.*;


@WebServlet(name = "TargetsRequestServlet", urlPatterns = {"/worker/targets-request"})
public class TargetsRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String taskNameFromParameter = request.getParameter(TASK_NAME);
        String username = SessionUtils.getUsername(request);
        Integer threadsCount = SessionUtils.getWorkerThreads(request);
        if (username != null || threadsCount != null) {

            if (taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            } else {
                NewEngine engine = ServletUtils.getEngine(getServletContext());
                if (engine.isRegistered(username, taskNameFromParameter)) {
                    if (engine.isExecActive(taskNameFromParameter)) {
                        //Execution is running
                        List<NewExecutionTargetDTO> newTargets = engine.requestExecutionTargets(taskNameFromParameter, threadsCount);
                        if (newTargets == null || newTargets.isEmpty()) {
                            // No targets are available:
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        } else {
                            try (PrintWriter out = response.getWriter()) {
                                // There are new targets available to send:
                                String targetAsJson = GSON_INST.toJson(newTargets);
                                out.println(targetAsJson);
                                out.flush();
                                response.setStatus(HttpServletResponse.SC_OK);
                            }
                        }
                    } else if (engine.isExecStopped(taskNameFromParameter)) {
                        response.setStatus(SC_EXEC_STOPPED);
                    } else if (engine.isExecDone(taskNameFromParameter)) {
                        response.setStatus(SC_EXEC_DONE);
                    }
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}