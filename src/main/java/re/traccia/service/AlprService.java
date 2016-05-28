package re.traccia.service;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import re.traccia.repository.MongoRepository;

import static re.traccia.management.AppConstants.*;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class AlprService extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(TracesService.class);
    private MongoRepository mongoRepository;
    private Router router;
    private Alpr alpr;

    public AlprService(Router router, MongoClient mongoClient) {
        this.router = router;
        this.mongoRepository = new MongoRepository(mongoClient);
        this.alpr = new Alpr(OPENALPR_COUNTRY, OPENALPR_CONF_PATH, OPENALPR_RUNTIME_DIR);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("start tracesService");
        startWebApp((start) -> {
            if (start.succeeded()) {
                completeStartup(start, startFuture);
            } else {
                System.out.println("error - startWebApp: " + start.cause().getMessage());
            }
        });
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            logger.info("Application started");
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
        mongoRepository.getImg(id, result -> {
            JsonObject imgObj = result.result();
            FileSystem fs = vertx.fileSystem();
            fs.writeFile(tmpImage, Buffer.buffer().appendBytes(imgObj.getBinary("img")), fsResult -> {
                if (fsResult.succeeded()) {
                    System.out.println("OK");
                    // Set top N candidates returned to 20
                    alpr.setTopN(20);
                    // Set pattern to Maryland
                    //        alpr.setDefaultRegion("md");

                    AlprResults results = alpr.recognize(tmpImage);
                    System.out.format("  %-15s%-8s\n", "Plate Number", "Confidence");
                    for (AlprPlateResult single : results.getPlates()) {
                        for (AlprPlate plate : single.getTopNPlates()) {
                            if (plate.isMatchesTemplate())
                                System.out.print("  * ");
                            else
                                System.out.print("  - ");
                            System.out.format("%-15s%-8f\n", plate.getCharacters(), plate.getOverallConfidence());
                        }
                    }
                    // Make sure to call this to release memory
                    alpr.unload();
                    fs.delete(tmpImage, delete -> {
                        if (delete.succeeded()) {
                            System.out.println("OK deleted");
                        } else
                            System.out.print(" NO DELETE - ");
                    });
                } else {
                    System.err.println("Oh oh ..." + fsResult.cause());
                }
            });
        });


    }

    private void end404(RoutingContext routingContext, String msg) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + msg)
                .end();
    }


}
