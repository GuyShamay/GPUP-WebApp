package gpup.servlets.worker.execution;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.target.FinishResultDTO;
import dto.target.FinishedTargetDTO;
import engine.NewEngine;
import gpup.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

@WebServlet(name = "SendTargetServlet", urlPatterns = {"/worker/send-target"})
public class SendTargetServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        Scanner s = new Scanner(req.getInputStream(), "UTF-8").useDelimiter("\\A");
        String reqBodyAsString = s.hasNext() ? s.next() : null;

        if (reqBodyAsString != null || !reqBodyAsString.isEmpty()) {
            FinishedTargetDTO finishedTarget = parseBodyToFinishedTarget(reqBodyAsString);
            NewEngine engine = ServletUtils.getEngine(getServletContext());
            engine.setFinishedTarget(finishedTarget);
        } else {
            try (PrintWriter out = resp.getWriter()) {
                String errorMessage = "You didnt send target! ";
                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                out.println(errorMessage);
                out.flush();
            }
        }
    }

    private FinishedTargetDTO parseBodyToFinishedTarget(String reqBodyAsString) {
        FinishedTargetDTO target = new FinishedTargetDTO();
            JsonObject jsonObject = JsonParser.parseString(reqBodyAsString).getAsJsonObject();
            if(jsonObject.has("name"))
                target.setName(jsonObject.get("name").getAsString());
            if(jsonObject.has("executionName"))
                target.setExecutionName(jsonObject.get("executionName").getAsString());
            if(jsonObject.has("logs"))
                target.setLogs(jsonObject.get("logs").getAsString());
            if(jsonObject.has("worker"))
                target.setWorker(jsonObject.get("worker").getAsString());
        if (jsonObject.has("processingTime"))
            target.setProcessingTime(jsonObject.get("processingTime").getAsString());
            if(jsonObject.has("finishResult"))
                target.setFinishResult(FinishResultDTO.valueOf(jsonObject.get("finishResult").getAsString()));

        return target;
    }

}
