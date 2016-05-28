package re.traccia.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import re.traccia.model.Trace;
import re.traccia.repository.MongoRepository;

import static re.traccia.management.AppConstants.TRACES_PATH;


/**
 * Created by fiorenzo on 28/05/16.
 */
public class TracesService extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(TracesService.class);
    private MongoRepository mongoRepository;
    private Router router;

    public TracesService(Router router, MongoClient mongoClient) {
        this.router = router;
        this.mongoRepository = new MongoRepository(mongoClient);
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
        router.get(TRACES_PATH).handler(this::getList);
        router.post(TRACES_PATH).handler(this::create);
        router.get(TRACES_PATH + ":id").handler(this::fetch);
        router.get(TRACES_PATH + ":id/image").handler(this::getImage);
        router.put(TRACES_PATH + ":id").handler(this::update);
        router.delete(TRACES_PATH + ":id").handler(this::delete);
        next.handle(Future.succeededFuture());
    }

    private void create(RoutingContext routingContext) {
        logger.info("CREATE: " + routingContext.getBodyAsString());
        Trace trace =
                Json.decodeValue(routingContext.getBodyAsString(),
                        Trace.class);
        FileSystem fs = vertx.fileSystem();
        fs.writeFile("docs/auto_up.jpg", Buffer.buffer().appendBytes(trace.getImage()), result -> {
            if (result.succeeded()) {
                System.out.println("OK");
            } else {
                System.err.println("Oh oh ..." + result.cause());
            }
        });
        mongoRepository.create(trace, single -> {
                    if (single.failed()) {
                        end404(routingContext);
                        return;
                    }
                    routingContext.response()
                            .setStatusCode(201)
                            .putHeader("content-type",
                                    "application/json; charset=utf-8")
                            .end(Json.encodePrettily(single.result()));
                }
        );

    }

    private void fetch(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext);
            return;
        }
        mongoRepository.fetch(id, result -> {
            if (result.failed()) {
                end404(routingContext);
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
            end404(routingContext);
            return;
        }
        mongoRepository.getImg(id, result -> {
            if (result.failed()) {
                end404(routingContext);
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
        Trace trace = Json.decodeValue(routingContext.getBodyAsString(),
                Trace.class);
        if (id == null || trace == null) {
            end404(routingContext);
            return;
        }
        mongoRepository.update(id, trace,
                updated -> {
                    if (updated.failed()) {
                        end404(routingContext);
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
            end404(routingContext);
            return;
        }
        mongoRepository.delete(id,
                deleted -> {
                    if (deleted.failed()) {
                        end404(routingContext);
                        return;
                    }
                    routingContext.response()
                            .setStatusCode(204).end();
                }
        );

    }

    private void getList(RoutingContext routingContext) {
        mongoRepository.list(
                list -> {
                    if (list.failed()) {
                        end404(routingContext);
                        return;
                    }
                    routingContext.response()
                            .putHeader("content-type",
                                    "application/json; charset=utf-8")
                            .end(Json.encodePrettily(list.result()));
                }
        );
    }

    private void end404(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + routingContext.getBodyAsString()).end();
    }


}


