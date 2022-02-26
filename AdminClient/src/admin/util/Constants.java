package admin.util;

import com.google.gson.Gson;

public class Constants {
    public final static int REFRESH_RATE = 1000;

    // FXML Locations:
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "component/main/admin-main-app.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "../login/login.fxml";
    public final static String ADMIN_DASHBOARD_FXML_RESOURCE_LOCATION = "../dashboard/admin-dashboard.fxml";
    public final static String TASK_CONTROL_FXML_RESOURCE_LOCATION = "../taskcontrol/task-control.fxml";
    public final static String FIND_PATH_FXML_RESOURCE_LOCATION = "../actions/path/find-paths.fxml";
    public final static String FIND_CIRCUIT_FXML_RESOURCE_LOCATION = "../actions/circuit/find-circuit.fxml";
    public final static String WHAT_IF_FXML_RESOURCE_LOCATION = "../actions/whatif/what-if.fxml";
    public final static String GRAPH_DATA_FXML_RESOURCE_LOCATION = "../actions/data/graph-data.fxml";
    public final static String TASKS_LIST_FXML_RESOURCE_LOCATION = "../tasks/tasks.fxml";
    public final static String TASK_FROM_GRAPH_CONFIG_FXML_RESOURCE_LOCATION = "config/graph-task.fxml";
    public final static String SIMULATION_CONFIG_FXML_NAME = "simulation/simulation-config.fxml";
    public final static String COMPILE_CONFIG_FXML_NAME = "compile/compile-config.fxml";
    // Gson:
    public final static Gson GSON_INST = new Gson();

    // Query Parameters:
    public static final String USERNAME = "username";
    public static final String GRAPH_NAME = "graph-name";
    public static final String TASK = "task";
    public static final String TARGET_NAME = "target-name";
    public static final String RELATION_TYPE = "relation-type";
    public static final String NO = "n";
    public static final String YES = "y";
    public static final String GRAPH_TASK = "graph-task";
    public static final String VERSION = "ver";

    // Server Resources:
    public final static String BASE_DOMAIN = "localhost";
    public final static String PORT = ":8080";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + PORT;
    private final static String CONTEXT_PATH = "/GPUPWebApp";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;
    private final static String ADMIN = "/admin";

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/adminLogin";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String UPLOAD_FILE = FULL_SERVER_PATH + ADMIN + "/upload-file";
    public final static String GRAPHS_LIST = FULL_SERVER_PATH + ADMIN + "/graph-list";
    public final static String FIND_PATHS = FULL_SERVER_PATH + ADMIN + "/find-paths";
    public final static String FIND_CIRCUIT = FULL_SERVER_PATH + ADMIN + "/find-circuit";
    public static final String GRAPH_TARGETS = FULL_SERVER_PATH + ADMIN + "/graph-targets";
    public static final String WHAT_IF = FULL_SERVER_PATH + ADMIN + "/what-if";
    public static final String UPLOAD_TASK = FULL_SERVER_PATH + ADMIN + "/upload-task";
    public static final String TASKS_LIST = FULL_SERVER_PATH + "/tasks-list";
    public static final String RUN_EXECUTION = FULL_SERVER_PATH + "/run-execution";
    public static final String PLAY_EXECUTION = FULL_SERVER_PATH + ADMIN + "/play-execution";
    public static final String EXECUTION_CONTROL = FULL_SERVER_PATH + ADMIN + "/execution-control";
    public static final String EXECUTION_LOGS = FULL_SERVER_PATH + ADMIN + "/execution-logs";
    public static final String TARGET_REALTIME = FULL_SERVER_PATH + ADMIN + "/execution-target";


}
