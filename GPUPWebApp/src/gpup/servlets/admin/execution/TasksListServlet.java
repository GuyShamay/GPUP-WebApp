package gpup.servlets.admin.execution;

import com.google.gson.Gson;
import com.sun.javaws.HtmlOptions;
import dto.execution.ExecutionDTO;
import dto.graph.GraphDTO;
import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "TasksListServlet", urlPatterns = {"/tasks-list"})
public class TasksListServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            NewEngine engine = ServletUtils.getEngine(getServletContext());
            List<ExecutionDTO> tasks = engine.getTasksDTO();
            String json = gson.toJson(tasks);
            out.println(json);
            out.flush();
        }
    }
}
