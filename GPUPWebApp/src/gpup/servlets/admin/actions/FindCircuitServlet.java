package gpup.servlets.admin.actions;

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

import static gpup.constants.Constants.*;

@WebServlet(name = "FindCircuitServlet", urlPatterns = {"/admin/find-circuit"})
public class FindCircuitServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        //response.setContentType("text/html;charset=UTF-8");

        NewEngine engine = ServletUtils.getEngine(getServletContext());
        String graphNameFromParameter = request.getParameter(GRAPH_NAME);
        String srcTargetFromParameter = request.getParameter(TARGET_NAME);
        try (PrintWriter out = response.getWriter()) {

            if (graphNameFromParameter == null || graphNameFromParameter.isEmpty() || srcTargetFromParameter == null || srcTargetFromParameter.isEmpty()) {
                String errorMessage = "No graph / source target was sent";
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                out.println(errorMessage);
                out.flush();
            } else {
                if (engine.isGraphExist(graphNameFromParameter)) {
                    List<String> circuit = engine.findCircuit(graphNameFromParameter, srcTargetFromParameter);
                    if (circuit == null) {
                        String errorMessage = "No Circuit found.";
                        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        out.println(errorMessage);
                        System.out.println(errorMessage);
                        out.flush();
                    } else {
                        String listAsJson = GSON_INST.toJson(circuit);
                        out.println(listAsJson);
                        System.out.println(listAsJson);
                        out.flush();
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                } else {
                    String errorMessage = "No graph named " + graphNameFromParameter + " in the system";
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    out.println(errorMessage);
                    out.flush();
                }

            }
        }
    }
}
