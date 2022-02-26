package gpup.servlets.admin.execution;


import dto.target.TargetDTO;
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

@WebServlet(name = "TargetRealTimeServlet", urlPatterns = {"/admin/execution-target"})
public class TargetRealTimeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processResponse(req, resp);
    }

    private void processResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
    String taskNameFromParam = req.getParameter(TASK);
    String targetNameFromParam = req.getParameter(TARGET_NAME);
        if (taskNameFromParam == null || taskNameFromParam.isEmpty()) {
        String errorMsg = "No Task was sent";
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        resp.getOutputStream().print(errorMsg);
    } else {
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        if (engine.isTaskInSystem(taskNameFromParam)) {
            try (PrintWriter out = resp.getWriter()) {

                TargetDTO targetDTO = engine.getTargetDTORealTime(taskNameFromParam,targetNameFromParam);
                String targetAsJson = GSON_INST.toJson(targetDTO);
                out.println(targetAsJson);
                out.flush();
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } else {
            String errorMsg = "No Task named " + taskNameFromParam + " in the system";
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getOutputStream().print(errorMsg);
        }
    }

}}
