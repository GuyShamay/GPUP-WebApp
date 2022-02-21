package gpup.servlets.admin.graph;

import com.google.gson.Gson;
import dto.graph.GraphDTO;
import dto.users.UserDTO;
import engine.NewEngine;
import engine.users.UserManager;
import gpup.util.ServletUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GraphListServlet", urlPatterns = {"/admin/graph-list"})
public class GraphListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            NewEngine engine = ServletUtils.getEngine(getServletContext());
            List<GraphDTO> graphs = engine.getGraphsDTO();
            String json = gson.toJson(graphs);
            out.println(json);
            out.flush();
        }
    }
}
