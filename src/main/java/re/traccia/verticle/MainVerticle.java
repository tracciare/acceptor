package re.traccia.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import re.traccia.service.AlprService;
import re.traccia.service.TracesService;

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

        TracesService tracesService = new TracesService(router, this.mongoClient);
        AlprService alprService = new AlprService(router, this.mongoClient);

        vertx.deployVerticle(tracesService, new DeploymentOptions().setConfig(config()));
        vertx.deployVerticle(alprService, new DeploymentOptions().setWorker(true));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", PORT)
                );

    }

    private static JsonObject mongoConfig() {
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
