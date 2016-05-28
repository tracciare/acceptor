package re.traccia.service;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import re.traccia.repository.TracesRepository;

import static re.traccia.management.AppConstants.*;


public class AlprService extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(AlprService.class);
    private TracesRepository tracesRepository;
    private Router router;
    private Alpr alpr;

    public AlprService(Router router, MongoClient mongoClient, Vertx vertx) {
        this.router = router;
        this.tracesRepository = new TracesRepository(mongoClient);
        this.alpr = new Alpr(OPENALPR_COUNTRY, OPENALPR_CONF_PATH, OPENALPR_RUNTIME_DIR);
        this.vertx = vertx;
        getVertx().eventBus().consumer("re.traccia.alpr", this::consume);
    }

    private <T> void consume(Message<T> message) {
        logger.info("received msg: " + message.body());
        String id = (String) message.body();
        this.tracesRepository.fetch(id, result -> {
            if (result.succeeded()) {
                logger.info(result.result());
            } else {
                logger.info("error - fecth trace: " + result.cause().getMessage());
            }

        });
    }

    public AlprService() {
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("AlprService start tracesService");
        startWebApp((start) -> {
            if (start.succeeded()) {
                completeStartup(start, startFuture);
            } else {
                logger.info("error - startWebApp: " + start.cause().getMessage());
            }
        });
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            logger.info("AlprService Application started");
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        router.get(ALPR_PATH + ":id").handler(this::decode);
        next.handle(Future.succeededFuture());
    }


    private void decode(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext, "no id");
            return;
        }
        String tmpImage = "/tmp/" + id + ".jpg";
        tracesRepository.image(id, result -> {
            JsonObject imgObj = result.result();
            FileSystem fs = vertx.fileSystem();
            fs.writeFile(tmpImage, Buffer.buffer().appendBytes(imgObj.getBinary("img")), fsResult -> {
                if (fsResult.succeeded()) {
                    logger.info("OK");
                    // Set top N candidates returned to 20
                    alpr.setTopN(20);
                    // Set pattern to Maryland
                    //        alpr.setDefaultRegion("md");

                    AlprResults results = alpr.recognize(tmpImage);
                    for (AlprPlateResult single : results.getPlates()) {
                        for (AlprPlate plate : single.getTopNPlates()) {
                            if (plate.isMatchesTemplate())
                                logger.info("  * ");
                            else
                                logger.info("  - ");
                            logger.info(plate.getCharacters() + ":" + plate.getOverallConfidence());
                        }
                    }
                    // Make sure to call this to release memory
                    fs.delete(tmpImage, delete -> {
                        if (delete.succeeded()) {
                            logger.info("OK deleted");
                        } else
                            logger.error(" NO DELETE - ");
                    });
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type",
                                    "application/json; charset=utf-8")
                            .end(Json.encodePrettily(results.getJobj()));
                } else {
                    logger.error("Oh oh ..." + fsResult.cause());
                }
            });
        });


    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        logger.info("stop AlprService");
        alpr.unload();
    }

    private void end404(RoutingContext routingContext, String msg) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + msg)
                .end();
    }


}
