package gpup.servlets.admin.graph;

import engine.NewEngine;
import engine.exceptions.CircuitInGraphException;
import engine.exceptions.GraphExistException;
import gpup.util.ServletUtils;
import gpup.util.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import javax.xml.bind.JAXBException;
import java.io.*;

import java.util.Collection;

import static gpup.constants.Constants.WORKING_DIR;

@WebServlet("/admin/upload-file")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class UploadFileServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/plain");
        Collection<Part> parts = request.getParts();
        String creatingUser = SessionUtils.getUsername(request);

        // Engine
        NewEngine engine = ServletUtils.getEngine(getServletContext());
        boolean isAdded = false;
        for (Part part : parts) {
            part.write(WORKING_DIR + part.getSubmittedFileName());
            File file = new File(WORKING_DIR + part.getSubmittedFileName());
            isAdded = addGraphToEngine(response, creatingUser, engine, file);
            if (!isAdded) break;
        }
        if (isAdded) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().print("Graph uploaded successfully!");
        }
    }

    private boolean addGraphToEngine(HttpServletResponse response, String creatingUser, NewEngine engine, File file) throws IOException {
        try {
            engine.parseFileToGraph(file, creatingUser);
        } catch (JAXBException e) {
            // failed file! not valid for system
            response.getOutputStream().print("Invalid File! Not compatible with GPUP schema");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            file.delete();
            return false;
        } catch (GraphExistException e) {
            response.getOutputStream().print(e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        } catch (CircuitInGraphException e) {
            response.getOutputStream().print(e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            file.delete();
            return false;
        }
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
