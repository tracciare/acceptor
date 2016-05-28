import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;
import re.traccia.verticle.MainVerticle;

import static re.traccia.management.AppConstants.TRACES_PATH;

/**
 * Created by fiorenzo on 28/05/16.
 */
@RunWith(VertxUnitRunner.class)
public class TracciaReVertxTest {

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName(),
                context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testIndex(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().getNow(AppConstants.PORT, "localhost", "/",
                response -> {
                    context.assertEquals(200, response.statusCode());
                    response.handler(body -> {
                        context.assertEquals("<html><body>hello!! magic jhonson</body></html>",
                                body.toString().replaceAll("\n", ""));
                        async.complete();
                    });
                });
    }

    @Test
    public void testCreateAndRemove(TestContext context) {
        final Async async = context.async();
        Trace trace = new Trace("lat", "lon", null, "123456");
        vertx.fileSystem().readFile("docs/auto.jpg", result -> {
            if (result.succeeded()) {
                trace.setImage(result.result().getBytes());
            } else {
                System.err.println("Oh oh ..." + result.cause());
            }
        });
        final String json = Json.encodePrettily(trace);
        final String length = Integer.toString(json.length());
        vertx.createHttpClient().post(AppConstants.PORT, "localhost", TRACES_PATH)
                .putHeader("content-type", "application/json")
                .putHeader("content-length", length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        String id = body.toJsonObject().getString("_id");
                        final Trace traceResult = Json.decodeValue(body.toString(), Trace.class);
                        context.assertEquals(traceResult.getLat(), "lat");
                        context.assertEquals(traceResult.getLon(), "lon");
                    });
                    async.complete();
                })
                .write(json)
                .end();
    }


}
