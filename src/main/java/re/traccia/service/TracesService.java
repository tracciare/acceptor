package re.traccia.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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

import static re.traccia.management.AppConstants.TRACES_PATH;


public class TracesService extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(TracesService.class);
    private Repository repository;
    private Router router;

    public TracesService() {
    }

    public TracesService(Router router, MongoClient mongoClient) {
        this.router = router;
        this.repository = new TracesRepository(mongoClient);
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
        byte[] img = trace.getImage();
        this.repository.create(trace.toJson(), single -> {
                    if (single.failed()) {
                        end404(routingContext, single.cause().getMessage());
                        return;
                    }
                    logger.info("_id: " + single.result());
                    ((TracesRepository) this.repository).createImage(img, single.result(), created -> {
                        if (created.failed()) {
                            end404(routingContext, single.cause().getMessage());
                            return;
                        }
                        routingContext.response()
                                .setStatusCode(200)
                                .putHeader("content-type",
                                        "application/json; charset=utf-8")
                                .end(Json.encodePrettily(created.result()));
                    });
                }
        );

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
                            .setStatusCode(204).end();
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


