package worker.client.util;

import com.google.gson.Gson;

public class Constants {
    public final static int REFRESH_RATE = 2000;
    public final static int CONTROL_REFRESH_RATE = 1000;
    public final static int TARGET_REQ_REFRESH_RATE = 500;

    // FXML Locations:
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "component/main/worker-main-app.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "../login/login.fxml";
    public final static String WORKER_DASHBOARD_FXML_RESOURCE_LOCATION = "../dashboard/worker-dashboard.fxml";
    public final static String TASKS_LIST_FXML_RESOURCE_LOCATION = "../tasks/list/tasks-list.fxml";
    public final static String TASK_CONTROL_FXML_RESOURCE_LOCATION = "tasks/task-control.fxml";
    public final static String TARGETS_CONTROL_FXML_RESOURCE_LOCATION = "targets/targets-control.fxml";
    public final static String CONTROL_PANEL_FXML_RESOURCE_LOCATION = "../control/control-panel.fxml";
    // Gson:
    public final static Gson GSON_INST = new Gson();

    // Parameters:
    public final static String TASK_NAME = "taskname";

    // Server Resources:
    public final static String BASE_DOMAIN = "localhost";
    public final static String PORT = ":8080";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + PORT;
    private final static String CONTEXT_PATH = "/GPUPWebApp_Web_exploded";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;
    private final static String WORKER = "/worker";
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/workerLogin";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String TASKS_LIST = FULL_SERVER_PATH + "/tasks-list";
    public static final String REGISTER = FULL_SERVER_PATH + WORKER + "/register";
    public static final String UNREGISTER = FULL_SERVER_PATH + WORKER + "/unregister";
    public static final String TARGET_REQUEST = FULL_SERVER_PATH + WORKER + "/targets-request";
    public static final String SEND_TARGET = FULL_SERVER_PATH + WORKER + "/send-target";
    public static final String LIGHT_W_EXEC = FULL_SERVER_PATH + WORKER + "/light-worker-exec";

}
