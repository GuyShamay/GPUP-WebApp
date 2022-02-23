package gpup.servlets.worker.execution;

import dto.execution.WorkerExecutionDTO;
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

import static gpup.constants.Constants.GSON_INST;
import static gpup.constants.Constants.TASK_NAME;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/worker/register"})
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String usernameFromSession = SessionUtils.getUsername(request);
        String taskNameFromParameter = request.getParameter(TASK_NAME);
        if (usernameFromSession != null) {
            try (PrintWriter out = response.getWriter()) {
                if (taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    String errorMessage = "No Task was sent to server.";
                    out.println(errorMessage);
                    out.flush();
                } else {
                    NewEngine engine = ServletUtils.getEngine(getServletContext());
                    synchronized (this) {
                        if (!engine.isRegistered(usernameFromSession, taskNameFromParameter)) {
                            engine.registerWorker(usernameFromSession, taskNameFromParameter);
                            WorkerExecutionDTO workerExecutionDTO = engine.getWorkerExecution(taskNameFromParameter);
                            String workerExecAsJson = GSON_INST.toJson(workerExecutionDTO);
                            out.println(workerExecAsJson);
                            out.flush();
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            String errorMessage = "You already registered to task: " + taskNameFromParameter;
                            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                            out.println(errorMessage);
                            out.flush();
                        }
                    }
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}