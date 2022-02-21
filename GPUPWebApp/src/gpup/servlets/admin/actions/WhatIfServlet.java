package gpup.servlets.admin.actions;

import dto.target.TargetDTO;
import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static gpup.constants.Constants.*;

@WebServlet(name = "WhatIfServlet", urlPatterns = {"/admin/what-if"})
public class WhatIfServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        NewEngine engine = ServletUtils.getEngine(getServletContext());
        String graphNameFromParameter = request.getParameter(GRAPH_NAME);
        String srcTargetFromParameter = request.getParameter(TARGET_NAME);
        String relationTypeFromParameter = request.getParameter(RELATION_TYPE);
        try (PrintWriter out = response.getWriter()) {

            if (graphNameFromParameter == null || graphNameFromParameter.isEmpty() || srcTargetFromParameter == null || srcTargetFromParameter.isEmpty()
                    || relationTypeFromParameter == null || relationTypeFromParameter.isEmpty()) {
                String errorMessage = "No graph / source target / relation type were sent";
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                out.println(errorMessage);
                out.flush();
            } else {
                if (engine.isGraphExist(graphNameFromParameter)) {
                    List<TargetDTO> whatif = engine.whatIf(graphNameFromParameter, srcTargetFromParameter, relationTypeFromParameter);
                    if (whatif == null || whatif.isEmpty()) {
                        String errorMessage = "No Targets are affected.";
                        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        out.println(errorMessage);
                        out.flush();
                    } else {
                        String listAsJson = GSON_INST.toJson(whatif);
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
