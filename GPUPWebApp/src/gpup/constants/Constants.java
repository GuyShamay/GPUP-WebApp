package gpup.constants;

import com.google.gson.Gson;
import org.omg.CORBA.PUBLIC_MEMBER;

public class Constants {
    // Gson:
    public final static Gson GSON_INST = new Gson();


    public static final String NO = "n";
    public static final String YES = "y";
    public static final String USERNAME = "username";
    public static final String GRAPH_NAME = "graph-name";
    public static final String TARGET_NAME = "target-name";
    public static final String RELATION_TYPE = "relation-type";
    public static final String GRAPH_TASK = "graph-task";
    public final static String TASK_NAME = "taskname";

    public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;
    public final static String WORKING_DIR = "C:\\gpup-working-dir\\";
}
