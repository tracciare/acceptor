package re.traccia.service;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
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
import re.traccia.model.Trace;
import re.traccia.common.Repository;
import re.traccia.repository.TracesRepository;

import java.time.Instant;
import java.util.Date;

import static re.traccia.management.AppConstants.*;


public class TracesService extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(TracesService.class);
    private Repository repository;
    private Router router;

    public TracesService() {
    }

    public TracesService(Router router, MongoClient mongoClient, Vertx vertx) {
        this.router = router;
        this.repository = new TracesRepository(mongoClient);
        this.vertx = vertx;
        MessageConsumer<String> consumer = getVertx().eventBus().consumer(TRACES_QUEUE);
        consumer.handler(this::consume);
    }

    private <T> void consume(Message<T> message) {
        logger.info("received msg: " + message.body());
    }


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("start TracesService");
        startWebApp(start -> {
            if (start.succeeded()) {
                completeStartup(start, startFuture);
            } else {
                System.out.println("error - startWebApp: " + start.cause().getMessage());
            }
        });
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            logger.info("TracesService Application started");
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        router.get(TRACES_PATH).handler(this::getList);
        router.post(TRACES_PATH).handler(this::create);
        router.get(TRACES_PATH + ":id").handler(this::fetch);
        router.get(TRACES_PATH + ":id/image").handler(this::getImage);
        router.put(TRACES_PATH + ":id").handler(this::update);
        router.delete(TRACES_PATH + ":id").handler(this::delete);
        next.handle(Future.succeededFuture());
    }

    private void create(RoutingContext routingContext) {
        Trace trace =
                Json.decodeValue(routingContext.getBodyAsString(),
                        Trace.class);
        trace.setStartDate(Instant.now());
        trace.setStatus(ACCEPTED);
        byte[] img = trace.getImage();
        JsonObject jsonObject = new JsonObject();
        Future<String> createTraceFuture = Future.future();
        Future<String> createImageFuture = Future.future();
        Future<Message<String>> end = Future.future();

        this.repository.create(trace.toJson(), createTraceFuture.completer());

        createTraceFuture.compose(traceId -> {
            logger.info("trace created with id: " + traceId);
            jsonObject.put("traceId", traceId);
            ((TracesRepository) this.repository).createImage(img, traceId, createImageFuture.completer());
        }, createImageFuture);

        createImageFuture.compose(imageId -> {
            logger.info("image created with id: " + jsonObject.getString("traceId"));
            jsonObject.put("imageId", imageId);
            getVertx().eventBus().publish(ALPR_QUEUE, jsonObject.getString("traceId"));
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type",
                            "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new JsonObject().put("_id", jsonObject.getString("traceId"))));
        }, end);

        end.setHandler(result -> {
            logger.info("msg not sent : " + result.succeeded());
            if (!result.succeeded()) {
                end404(routingContext, end.cause().getMessage());
                return;
            } else {
                logger.info("exit");
            }
        });

//        createImageFuture.compose(imageId -> {
//            jsonObject.put("imageId", imageId);
//            getVertx().eventBus().send(ALPR_QUEUE, jsonObject.getString("traceId"), ar -> {
//                logger.info("WE SENT MESSAGE!!!");
//            });
//        }, Future.succeededFuture());
//        createImageFuture.setHandler(result -> {
//            if (result.succeeded()) {
//                routingContext.response()
//                        .setStatusCode(200)
//                        .putHeader("content-type",
//                                "application/json; charset=utf-8")
//                        .end(Json.encodePrettily(new JsonObject().put("_id", jsonObject.getString("traceId"))));
//            } else {
//                end404(routingContext, createImageFuture.cause().getMessage());
//                return;
//            }
//        });
    }

    private void fetch(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext, "no id");
            return;
        }
        this.repository.fetch(id, result -> {
            if (result.failed()) {
                end404(routingContext, result.cause().getMessage());
                return;
            }
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type",
                            "application/json; charset=utf-8")
                    .end(Json.encodePrettily(result.result()));
        });
    }

    private void getImage(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext, "no id");
            return;
        }
        ((TracesRepository) repository).image(id, result -> {
            if (result.failed()) {
                end404(routingContext, result.cause().getMessage());
                return;
            }
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type",
                            "application/json; charset=utf-8")
                    .end(Json.encodePrettily(result.result()));
        });
    }

    private void update(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext, "no id");
            return;
        }
        this.repository.update(id, routingContext.getBodyAsJson(),
                updated -> {
                    if (updated.failed()) {
                        end404(routingContext, updated.cause().getMessage());
                        return;
                    }
                    routingContext.response()
                            .putHeader("content-type",
                                    "application/json; charset=utf-8")
                            .end(Json.encodePrettily(updated.result()));

                });
    }

    private void delete(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext, "no id");
            return;
        }
        this.repository.delete(id,
                deleted -> {
                    if (deleted.failed()) {
                        end404(routingContext, deleted.cause().getMessage());
                        return;
                    }
                    routingContext.response()
                            .setStatusCode(200).end();
                }
        );

    }

    private void getList(RoutingContext routingContext) {
        this.repository.list(new JsonObject(),
                list -> {
                    if (list.failed()) {
                        end404(routingContext, list.cause().getMessage());
                        return;
                    }
                    routingContext.response()
                            .putHeader("content-type",
                                    "application/json; charset=utf-8")
                            .end(Json.encodePrettily(list.result()));
                }
        );
    }

    private void end404(RoutingContext routingContext, String msg) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + msg)
                .end();
    }


}


