package gpup.servlets.admin.execution;

import dto.util.DTOUtil;
import engine.NewEngine;
import gpup.util.ServletUtils;
import gpup.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Scanner;

import static gpup.constants.Constants.TASK;

@WebServlet(name = "ExecutionControlServlet", urlPatterns = {"/admin/execution-control"})
public class ExecutionControlServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        String taskNameFromParameter = request.getParameter(TASK);
        String username = SessionUtils.getUsername(request);

        Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
        String reqBodyAsString = s.hasNext() ? s.next() : null;

        if (taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
            String errorMessage = "No task name was sent to server";
            response.getOutputStream().print(errorMessage);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            DTOUtil.ExecutionControlDTO command = parseToCommand(reqBodyAsString);
            if (command != null) {
                synchronized (this) {
                    if (engine.isExecutionExist(taskNameFromParameter)) {
                        if (engine.isExecutionCreatingUser(taskNameFromParameter, username)) {
                            try {
                                switch (command) {
                                    case Pause:
                                        engine.pauseExecution(taskNameFromParameter);
                                        break;
                                    case Resume:
                                        engine.resumeExecution(taskNameFromParameter);
                                        break;
                                    case Stop:
                                        engine.stopExecution(taskNameFromParameter);
                                        break;
                                }
                                response.setStatus(HttpServletResponse.SC_OK);
                            } catch (RuntimeException ex) {
                                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                                response.getOutputStream().print(ex.getMessage());
                            }
                        }
                    } else {
                        String errorMessage = "You are not allowed to run this task";
                        response.getOutputStream().print(errorMessage);
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                    }
                }
            } else {
                String errorMessage = "No Execution control command was sent to server";
                response.getOutputStream().print(errorMessage);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            }
        }
    }

    private DTOUtil.ExecutionControlDTO parseToCommand(String s) {
        return DTOUtil.ExecutionControlDTO.valueOf(s);
    }
}
