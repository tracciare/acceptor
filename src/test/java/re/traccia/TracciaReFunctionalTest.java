package re.traccia;

import com.jayway.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;
import re.traccia.utils.FunctionalTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RunWith(VertxUnitRunner.class)
public class TracciaReFunctionalTest extends FunctionalTestUtils {

    public final static String HOST = "192.168.99.100"; // $(docker-machine ip)
    public final static int PORT = 8080;
    public final static String TEST_IMAGE = "docs/images/car.jpg"; // $(docker-machine ip)

    private Vertx vertx;

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://" + HOST;
        RestAssured.port = Integer.getInteger("http.port", PORT);
    }

    @AfterClass
    public static void resetRestAssured() {
        RestAssured.reset();
    }

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
        vertx.createHttpClient().getNow(AppConstants.PORT, HOST, "/",
                response -> {
                    context.assertEquals(200, response.statusCode());
                    response.handler(body -> {
                        context.assertEquals("<html><body>Hello!! I'm Tracciare!</body></html>",
                                body.toString().replaceAll("\n", ""));
                        async.complete();
                    });
                });
    }

    @Test
    public void createProcessDeleteSuccess() throws Exception {
        Path path = Paths.get(TEST_IMAGE);
        byte[] data = Files.readAllBytes(path);
        Trace trace = new Trace("lat", "lon", data, null);

        createProcessDelete(trace);
    }

    public static String createProcessDelete(Trace trace) throws Exception {
        String newTraceId = null;
        try {
            newTraceId = createTrace(trace);
            Thread.sleep(10);  //wait for ALPR processing to happen
            String status = getTraceStatus(newTraceId);
            Assert.assertEquals(AppConstants.PROCESSED, status);
        } finally {
            deleteTrace(newTraceId);
        }
        return newTraceId;
    }

}
