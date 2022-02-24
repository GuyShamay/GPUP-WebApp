package gpup.util;

import gpup.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {
    public static String getUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static Integer getWorkerThreads(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.THREADS) : null;
        return sessionAttribute != null ? Integer.parseInt(sessionAttribute.toString()) : null;
    }

    public static void clearSession(HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
