package gpup.servlets.worker.execution;

import dto.target.FinishedTargetDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Scanner;

@WebServlet(name = "SendTargetServlet", urlPatterns = {"/worker/send-targets"})
public class SendTargetServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        Scanner s = new Scanner(req.getInputStream(), "UTF-8").useDelimiter("\\A");
        String reqBodyAsString = s.hasNext() ? s.next() : null;

        FinishedTargetDTO finishedTarget = parseBodyToFinishedTarget(reqBodyAsString);
    }

    private FinishedTargetDTO parseBodyToFinishedTarget(String reqBodyAsString) {
        ///TODOOOO
      //  FinishedTargetDTO target = new FinishedTargetDTO()
        return null;
    }

}
