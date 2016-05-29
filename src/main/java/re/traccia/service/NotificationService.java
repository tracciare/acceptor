package re.traccia.service;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import re.traccia.repository.UsersRepository;

import static re.traccia.management.AppConstants.NOTIFICATIONS_PATH;
import static re.traccia.management.AppConstants.NOTIFICATION_QUEUE;

/**
 * Created by fiorenzo on 29/05/16.
 */
public class NotificationService extends AbstractVerticle

{

    private final static Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private UsersRepository usersRepository;
    private Router router;

    public NotificationService() {
    }

    public NotificationService(Router router, MongoClient mongoClient, Vertx vertx) {
        this.router = router;
        this.usersRepository = new UsersRepository(mongoClient);
        this.vertx = vertx;
        MessageConsumer<String> consumer = getVertx().eventBus().consumer(NOTIFICATION_QUEUE);
        consumer.handler(this::consume);
    }

    private <T> void consume(Message<T> message) {
        logger.info("received msg: " + message.body());
        JsonObject msg = (JsonObject) message.body();
    }


    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("NotificationService start");
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
        router.post(NOTIFICATIONS_PATH + ":id").handler(this::notify);
        next.handle(Future.succeededFuture());
    }

    private void notify(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            end404(routingContext, "no id");
            return;
        }
        JsonObject jsonObject = routingContext.getBodyAsJson();

        MailClient mailClient = MailClient.createNonShared(vertx, getMailConfig());


        mailClient.sendMail(getMailMessage(jsonObject), result -> {
            if (result.succeeded()) {
                logger.info(result.result());
            } else {
                result.cause().printStackTrace();
            }
        });
    }

    private MailMessage getMailMessage(JsonObject jsonObject) {
        MailMessage mailMessage = new MailMessage();
//        mailMessage.setFrom("user@example.com (Example User)");
        mailMessage.setFrom(jsonObject.getString("from"));
//        mailMessage.setTo("recipient@example.org");
        mailMessage.setTo(jsonObject.getString("to"));
//        mailMessage.setCc("Another User <another@example.net>");
        mailMessage.setCc(jsonObject.getString("cc"));
        mailMessage.setSubject(jsonObject.getString("subject"));
        mailMessage.setText(jsonObject.getString("text"));
        return mailMessage;
    }

    private MailConfig getMailConfig() {
        MailConfig mailConfig = new MailConfig();
        mailConfig.setHostname("mail.example.com");
        mailConfig.setPort(587);
        mailConfig.setStarttls(StartTLSOptions.REQUIRED);
        mailConfig.setUsername("user");
        mailConfig.setPassword("password");
        return mailConfig;
    }


    private void end404(RoutingContext routingContext, String msg) {
        routingContext.response()
                .setStatusCode(404).setStatusMessage("ERROR CONTEXT: " + msg)
                .end();
    }


}
