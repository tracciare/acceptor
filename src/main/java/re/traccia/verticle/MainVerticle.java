package re.traccia.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import re.traccia.service.AlprService;
import re.traccia.service.ParkingSlotsService;
import re.traccia.service.TracesService;
import re.traccia.service.UsersService;

import static re.traccia.management.AppConstants.*;


public class MainVerticle extends AbstractVerticle {


    private MongoClient mongoClient;

    static {
        try {
            // Load the OpenALPR library at runtime
            // openalprjni.dll (Windows) or libopenalprjni.so (Linux/Mac)
            System.loadLibrary("openalprjni");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
        }
    }


    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        mongoClient = MongoClient.createShared(vertx, mongoConfig(), "tracepool");
        router.route("/").handler(StaticHandler.create("assets"));
        router.route("/api*").handler(BodyHandler.create());

        AlprService alprService = new AlprService(router, this.mongoClient);
        ParkingSlotsService parkingSlotsService = new ParkingSlotsService(router, this.mongoClient);
        TracesService tracesService = new TracesService(router, this.mongoClient);
        UsersService usersService = new UsersService(router, this.mongoClient);
        vertx.deployVerticle(alprService, new DeploymentOptions().setWorker(true));

        vertx.deployVerticle(parkingSlotsService, new DeploymentOptions().setConfig(config()));
        vertx.deployVerticle(usersService, new DeploymentOptions().setConfig(config()));
        vertx.deployVerticle(tracesService, new DeploymentOptions().setConfig(config()));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", PORT)
                );

    }

    public static JsonObject mongoConfig() {
        JsonObject config = new JsonObject();
        config.put("host", MONGO_HOST);
        config.put("port", MONGO_PORT);
        config.put("db_name", MONGO_DB_NAME);
        return config;
    }

    @Override
    public void stop() throws Exception {
    }
}
