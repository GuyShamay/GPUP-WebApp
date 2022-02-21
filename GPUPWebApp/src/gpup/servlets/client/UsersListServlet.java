package gpup.servlets.client;

import com.google.gson.Gson;
import dto.users.UserDTO;
import gpup.constants.Constants;
import gpup.util.ServletUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import engine.users.UserManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static gpup.constants.Constants.GSON_INST;

@WebServlet(name = "UsersListServlet", urlPatterns = {"/userslist"})
public class UsersListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            List<UserDTO> users = userManager.getUsersDTO();
            String json = GSON_INST.toJson(users);
            out.println(json);
            out.flush();
        }
    }

}
