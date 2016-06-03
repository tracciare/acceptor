package re.traccia.service;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static re.traccia.management.AppConstants.NOTIFICATIONS_PATH;
import static re.traccia.management.AppConstants.SERVICE_TEST_PATH;

/**
 * Created by fiorenzo on 02/06/16.
 */
public class ServiceTestRs extends AbstractVerticle

{

    private final static Logger logger = LoggerFactory.getLogger(ServiceTestRs.class);
    private Router router;
    private ServiceTest serviceTest;

    public ServiceTestRs() {
    }

    public ServiceTestRs(Router router, Vertx vertx) {
        this.router = router;
        this.vertx = vertx;
        serviceTest = ServiceTest.createProxy(vertx, ServiceTest.class.getName());

    }


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("ServiceTestRs start");
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
            logger.info("ServiceTestRs Application started");
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        router.get(SERVICE_TEST_PATH).handler(this::call);
        next.handle(Future.succeededFuture());
    }

    private void call(RoutingContext routingContext) {

        // Save some data in the database - this time using the proxy
        serviceTest.process(new JsonObject().put("name", "tim"), result -> {
            if (result.succeeded()) {
                logger.info("OK");
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type",
                                "application/json; charset=utf-8")
                        .end(Json.encodePrettily(result.result()));
            } else {
                logger.info("no proxy");
                result.cause().printStackTrace();
                end404(routingContext, "error in notification");
            }
        });
    }

    private void end404(RoutingContext routingContext, String msg) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + msg)
                .end();
    }

}
