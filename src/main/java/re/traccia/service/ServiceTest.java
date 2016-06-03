package re.traccia.service;

/**
 * Created by fiorenzo on 02/06/16.
 */

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
public interface ServiceTest {

    void process(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    static ServiceTest create(Vertx vertx) {
        System.out.println("ServiceTest.create");
        return new ServiceTestImpl();
    }

    static ServiceTest createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(ServiceTest.class, vertx, address);
    }
}
