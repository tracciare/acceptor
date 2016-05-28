package re.traccia.management;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class AppConstants {

    public static final String APP_CONTEST = "/api/";
    public static final String TRACES_PATH = APP_CONTEST + "traces/";
    public static final String USERS_PATH = APP_CONTEST + "users/";
    public static final String PARKING_SLOTS_PATH = APP_CONTEST + "parkingslots/";
    public static final String ALPR_PATH = APP_CONTEST + "alpr/";

    public static final int MONGO_PORT = 27017;
    public static final String MONGO_HOST = "192.168.99.100";
    public static final String MONGO_DB_NAME = "traces-db";
    public static final int PORT = 8080;


    public static final String TRACES = "traces";
    public static final String IMAGES = "images";

    public static final String OPENALPR_CONF_PATH = "openalpr.conf";
    public static final String OPENALPR_COUNTRY = "eu";
    public static final String OPENALPR_RUNTIME_DIR = "/usr/share/openalpr/runtime_data";
}
