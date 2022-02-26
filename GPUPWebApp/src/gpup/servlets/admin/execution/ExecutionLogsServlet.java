package gpup.servlets.admin.execution;

import com.google.gson.Gson;
import dto.execution.LogsWithVersion;
import engine.NewEngine;
import gpup.constants.Constants;
import gpup.servlets.client.RunExecutionServlet;
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

@WebServlet(name = "ExecutionLogsServlet", urlPatterns = {"/admin/execution-logs"})
public class ExecutionLogsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        String username = SessionUtils.getUsername(request);
        String taskNameFromParameter = request.getParameter(TASK);
        int logsVersion = 0;

        if (username == null || taskNameFromParameter == null || taskNameFromParameter.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        try {
            logsVersion = Integer.parseInt(request.getParameter(VERSION));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }


        NewEngine engine = ServletUtils.getEngine(getServletContext());
        List<String> logsEntries = null;
        int executionLogsVersion = 0;
        synchronized (this) {
            if (engine.isExecutionExist(taskNameFromParameter)) {
                executionLogsVersion = engine.getExecutionLogsVersion(taskNameFromParameter);
                logsEntries = engine.getExecutionLogsEntries(taskNameFromParameter, logsVersion);
            }
        }

        LogsWithVersion lwv = new LogsWithVersion();
        lwv.setEntries(logsEntries);
        lwv.setVersion(executionLogsVersion);

        String jsonResponse = GSON_INST.toJson(lwv);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}