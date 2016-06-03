package re.traccia.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by fiorenzo on 02/06/16.
 */
public class ServiceTestImpl implements ServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(ServiceTestImpl.class);

    @Override
    public void process(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        logger.info("ServiceTestImpl: " + document.toString());
        resultHandler.handle(Future.succeededFuture());
    }
}
