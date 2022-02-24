package gpup.servlets.worker.execution;

import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static gpup.constants.Constants.TASK_NAME;


@WebServlet(name = "TargetsRequestServlet", urlPatterns = {"/worker/targets-request"})
public class TargetsRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String taskNameFromParameter = request.getParameter(TASK_NAME);
        NewEngine engine = ServletUtils.getEngine(getServletContext());



    }
}