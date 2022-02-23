package gpup.servlets.admin.execution;

import engine.NewEngine;
import gpup.util.ServletUtils;
import gpup.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static gpup.constants.Constants.TASK;
import static gpup.constants.Constants.USERNAME;

@WebServlet(name = "PlayExecutionServlet", urlPatterns = {"/admin/play-execution"})
public class PlayExecutionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        String taskNameFromParameter = request.getParameter(TASK);
        String username = SessionUtils.getUsername(request);
        if (taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
            String errorMessage = "Conflict in Server state";
            response.getOutputStream().print(errorMessage);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            synchronized (this) {
                if (engine.isExecutionExist(taskNameFromParameter)) {
                    if (engine.isExecutionCreatingUser(taskNameFromParameter, username)) {
                        if (engine.isAllowedToPlayExecution(taskNameFromParameter)) {
                            try {
                                engine.playExecution(taskNameFromParameter);
                                response.setStatus(HttpServletResponse.SC_OK);
                            } catch (RuntimeException ex) { // circuit handling
                                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                                response.getOutputStream().print(ex.getMessage());
                            }
                        } else {
                            String errorMessage = "Can't Run task: already started/finished";
                            response.getOutputStream().print(errorMessage);
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                        }
                    } else {
                        String errorMessage = "You are not allowed to run this task";
                        response.getOutputStream().print(errorMessage);
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                    }
                }
            }
        }
    }
}
