package re.traccia.management;

import io.vertx.ext.mail.StartTLSOptions;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class AppConstants {
    //trace stautus
    public static final String PROCESSED = "processed";

    // mongodb
    public static final int MONGO_PORT = 27017;
    public static final String MONGO_HOST = "192.168.99.100";
    public static final String MONGO_DB_NAME = "traces-db";
    public static final int PORT = 8080;

    // mongodb collections
    public static final String IMAGES = "images";
    public static final String PARKINGSLOTS = "parkingslots";
    public static final String TRACES = "traces";
    public static final String USERS = "users";

    //rest path
    public static final String APP_CONTEST = "/api/";
    public static final String ALPR_PATH = APP_CONTEST + "alpr/";
    public static final String NOTIFICATIONS_PATH = APP_CONTEST + "notifications/";
    public static final String PARKING_CHECKER_PATH = APP_CONTEST + "parkingchecker/";
    public static final String PARKING_SLOTS_PATH = APP_CONTEST + PARKINGSLOTS + "/";
    public static final String TRACES_PATH = APP_CONTEST + TRACES + "/";
    public static final String USERS_PATH = APP_CONTEST + USERS + "/";


    public static final String OPENALPR_CONF_PATH = "openalpr.conf";
    public static final String OPENALPR_COUNTRY = "eu";
    public static final String OPENALPR_RUNTIME_DIR = "/usr/share/openalpr/runtime_data";

    //msg queues
    public static final String ALPR_QUEUE = "re.traccia.alpr";
    public static final String NOTIFICATION_QUEUE = "re.traccia.notifications";
    public static final String PARKING_CHECKER_QUEUE = "re.traccia.parkingchecker";
    public static final String PARKINGSLOTS_QUEUE = "re.traccia.parkingslots";
    public static final String TRACES_QUEUE = "re.traccia.traces";
    public static final String USERS_QUEUE = "re.traccia.users";


    // system email (for notifications)
    public static final String SYSTEM_EMAIL_FROM = "info@traccia.re";
    public static final String SYSTEM_EMAIL_CONTROLLER_TO = "fiorenzino@gmail.com";
    public static final String SYSTEM_EMAIL_CONTROLLER_CC = "fabio.cognigni@gmail.com";

    //mail smtp server
    public static final String MAIL_HOSTNAME = "smtp.gmail.com";
    public static final int MAIL_PORT = 587;
    public static final StartTLSOptions MAIL_STARTTLS = StartTLSOptions.REQUIRED;
    public static final String MAIL_USERNAME = "xxx@gmail.com";
    public static final String MAIL_PASSWORD = "xxx";

}
