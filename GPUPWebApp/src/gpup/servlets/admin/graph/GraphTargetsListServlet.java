package gpup.servlets.admin.graph;

import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static gpup.constants.Constants.GRAPH_NAME;
import static gpup.constants.Constants.GSON_INST;

@WebServlet("/admin/graph-targets")
public class GraphTargetsListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String graphNameFromParameter = request.getParameter(GRAPH_NAME);
        if (graphNameFromParameter == null || graphNameFromParameter.isEmpty()) {
            String errorMsg = "No graph was sent";
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getOutputStream().print(errorMsg);
        } else {
            NewEngine engine = ServletUtils.getEngine(getServletContext());
            if (engine.isGraphExist(graphNameFromParameter)) {
                try (PrintWriter out = response.getWriter()) {
                    List<String> targetsList = engine.getGraphTargetListByName(graphNameFromParameter);
                    String listAsJson = GSON_INST.toJson(targetsList);
                    out.println(listAsJson);
                    out.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                String errorMsg = "No graph named " + graphNameFromParameter + " in the system";
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getOutputStream().print(errorMsg);
            }
        }
    }
}
