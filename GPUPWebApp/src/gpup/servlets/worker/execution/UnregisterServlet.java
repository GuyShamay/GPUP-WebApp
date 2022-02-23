package gpup.servlets.worker.execution;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.execution.config.ExecutionConfigDTO;
import engine.NewEngine;
import engine.users.UserManager;
import gpup.util.ServletUtils;
import gpup.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static gpup.constants.Constants.YES;

@WebServlet(name = "UnregisterServlet", urlPatterns = {"/worker/unregister"})
public class UnregisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String username = SessionUtils.getUsername(request);
        Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
        String reqBodyAsString = s.hasNext() ? s.next() : null;

        List<String> list = parseBodyToTasksListByName(reqBodyAsString);
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (list != null) {
            synchronized (this) {
                if (userManager.isUserExists(username)) {
                    list.forEach(task -> engine.unregisterWorker(username, task));
                }
            }
        }
    }

    private List<String> parseBodyToTasksListByName(String reqBodyAsString) {
        if (reqBodyAsString != null || !reqBodyAsString.isEmpty()) {
            JsonArray jsonArray = JsonParser.parseString(reqBodyAsString).getAsJsonArray();
            List<String> list = new ArrayList<>();
            jsonArray.forEach(jsonElement -> {
                list.add(jsonElement.getAsString());
            });
            return list;
        }
        return null;
    }
}