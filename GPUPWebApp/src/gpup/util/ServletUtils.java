package gpup.util;

import engine.NewEngine;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import engine.users.UserManager;

import static gpup.constants.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String ENGINE_ATTRIBUTE_NAME = "gpupEngine";

    private static final Object userManagerLock = new Object();
    private static final Object engineLock = new Object();

    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                NewEngine engine = getEngine(servletContext);
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, engine.getUserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return INT_PARAMETER_ERROR;
    }

    public static NewEngine getEngine(ServletContext servletContext) {
        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new NewEngine());
            }
        }
        return (NewEngine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }
}
