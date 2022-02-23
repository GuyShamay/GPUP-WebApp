package gpup.servlets.client;

import engine.NewEngine;
import engine.users.UserManager;
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

import static gpup.constants.Constants.GSON_INST;
import static gpup.constants.Constants.USERNAME;

@WebServlet(name = "ExecutionProgressServlet", urlPatterns = {"/execution-progress"})
public class ExecutionProgressServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        //user is not logged
        String taskNameFromParameter = request.getParameter(USERNAME);
        if (taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
            String errorMessage = "Conflict in Server state";
            response.getOutputStream().print(errorMessage);
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            synchronized (this){
                if(engine.isExecutionExist(taskNameFromParameter)){
                    try (PrintWriter out = response.getWriter()) {
                       Double progress = engine.getExecutionProgress(taskNameFromParameter);
                        out.println(progress.toString());
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
