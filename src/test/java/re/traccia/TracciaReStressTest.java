package re.traccia;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import re.traccia.model.Trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jayway.restassured.RestAssured.given;


@RunWith(VertxUnitRunner.class)
public class TracciaReStressTest {

    public final static String HOST = TracciaReFunctionalTest.HOST;
    public final static int PORT = TracciaReFunctionalTest.PORT;
    public final static String TEST_IMAGE = TracciaReFunctionalTest.TEST_IMAGE;

    final int REQUESTS_PER_THREAD = 3;
    final int THREADS = 3;

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://" + HOST;
        RestAssured.port = Integer.getInteger("http.port", PORT);
    }

    @AfterClass
    public static void resetRestAssured() {
        RestAssured.reset();
    }

    protected Trace buildTrace() throws IOException {
        Path path = Paths.get(TEST_IMAGE);
        byte[] data = Files.readAllBytes(path);
        return new Trace("lat", "lon", data, null);
    }

    @Test
    public void sequentialStressTest() throws Exception {
        sendSequentialRequests(REQUESTS_PER_THREAD);
    }

    /**
     * @return List of all trace ids processed
     */
    protected List<String> sendSequentialRequests(int numberRequests) throws Exception {
        List<String> processedTraces = new ArrayList<>();
        for(int i=0; i < numberRequests; i++) {
            processedTraces.add(TracciaReFunctionalTest.createProcessDelete(buildTrace()));
            System.out.println("Done " + i);
        }
        return processedTraces;
    }

    @Test
    public void parallelStressTest() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        List<Callable<List<String>>> callables = new ArrayList<>();
        for(int i=0; i < THREADS; i++) {
            callables.add(() -> sendSequentialRequests(REQUESTS_PER_THREAD));
        }

        executor.invokeAll(callables)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach( processedTracesId -> {
                    Assert.assertEquals(REQUESTS_PER_THREAD, processedTracesId.size());
                });
    }

}
