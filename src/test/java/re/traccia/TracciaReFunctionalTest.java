package re.traccia;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import re.traccia.management.AppConstants;


@RunWith(VertxUnitRunner.class)
public class TracciaReFunctionalTest {

    public final String host = "192.168.99.100"; // $(docker-machine ip)
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testIndex(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().getNow(AppConstants.PORT, host, "/",
                response -> {
                    context.assertEquals(200, response.statusCode());
                    response.handler(body -> {
                        context.assertEquals("<html><body>hello!! magic jhonson</body></html>",
                                body.toString().replaceAll("\n", ""));
                        async.complete();
                    });
                });
    }


}
