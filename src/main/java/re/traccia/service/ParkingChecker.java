package re.traccia.service;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;
import re.traccia.repository.ParkingSlotsRepository;
import re.traccia.repository.UsersRepository;

import static re.traccia.management.AppConstants.*;

/**
 * Created by fiorenzo on 29/05/16.
 */
public class ParkingChecker extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(ParkingChecker.class);
    private ParkingSlotsRepository parkingSlotsRepository;
    private UsersRepository usersRepository;
    private Router router;

    public ParkingChecker() {
    }

    public ParkingChecker(Router router, MongoClient mongoClient, Vertx vertx) {
        this.router = router;
        this.parkingSlotsRepository = new ParkingSlotsRepository(mongoClient);
        this.usersRepository = new UsersRepository(mongoClient);
        this.vertx = vertx;
        MessageConsumer<String> consumer = getVertx().eventBus().consumer(PARKING_CHECKER_QUEUE);
        consumer.handler(this::consume);
    }

    private <T> void consume(Message<T> message) {
        logger.info("received msg: " + message.body());
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("ParkingChecker start");
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
            logger.info("NotificationService Application started");
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        router.post(PARKING_CHECKER_PATH + ":platenumber").handler(this::check);
        next.handle(Future.succeededFuture());
    }

    private void check(String platenumber, Handler<AsyncResult<JsonObject>> next) {
        parkingSlotsRepository.findByPlatenumber(platenumber, parkingslot -> {
            if (parkingslot.succeeded()) {
                // nothing to do (happy path)
                logger.info("parkingslot FOUND..OK! happy path!!");
            } else {
                // if the parking slot doesn't exist,
                usersRepository.findByPlatenumber(platenumber, userFound -> {
                    if (userFound.succeeded()) {
                        String userEmail = userFound.result().getString("email");
                        // send a notification to user
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("from", AppConstants.SYSTEM_EMAIL_FROM);
                        jsonObject.put("to", userEmail);
                        jsonObject.put("subject", platenumber + ": the parking slot doesn't exist!");
                        jsonObject.getString("text", "Please pay to avoid a fine.");
                        getVertx().eventBus().publish(NOTIFICATION_QUEUE, jsonObject);

                    } else {
                        // send a notification to the human controller
                        //we need the trace to found
                        Trace trace = new Trace();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("from", AppConstants.SYSTEM_EMAIL_FROM);
                        jsonObject.put("to", AppConstants.SYSTEM_EMAIL_CONTROLLER_TO);
                        jsonObject.put("cc", AppConstants.SYSTEM_EMAIL_CONTROLLER_CC);
                        jsonObject.put("subject", platenumber + ": the parking slot doesn't exist!");
                        String text = "this is the location - lat: " + trace.getLat() + " lon: " + trace.getLon();
                        jsonObject.getString("text", text);
                        getVertx().eventBus().publish(NOTIFICATION_QUEUE, jsonObject);
                    }
                });

            }
        });
        next.handle(Future.succeededFuture());
    }

    private void check(RoutingContext routingContext) {
        String platenumber = routingContext.request().getParam("platenumber");
        if (platenumber == null || platenumber.isEmpty()) {
            end404(routingContext, "no platenumber");
            return;
        }
        check(platenumber, result -> {
            if (result.succeeded()) {
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type",
                                "application/json; charset=utf-8")
                        .end(Json.encodePrettily(result.result()));
            } else {
                end404(routingContext, "error in parking checking");
            }
        });

    }


    private void end404(RoutingContext routingContext, String msg) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + msg)
                .end();
    }


}
