package gpup.servlets.worker.execution;

import dto.execution.LightWorkerExecution;
import engine.NewEngine;
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

@WebServlet(name = "LightWorkerExecutionServlet", urlPatterns = {"/worker/light-worker-exec"})
public class LightWorkerExecutionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String workerName = SessionUtils.getUsername(req);
        if (workerName == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        synchronized (this) {
            if (!engine.getUserManager().isUserExists(workerName)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        List<LightWorkerExecution> list = engine.getLightWorkerExecutions(workerName);
        String listAsString = GSON_INST.toJson(list);
        try (PrintWriter out = resp.getWriter()) {
            out.println(listAsString);
            out.flush();
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
