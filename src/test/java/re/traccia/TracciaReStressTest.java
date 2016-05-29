package re.traccia;

import com.jayway.restassured.RestAssured;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;
import re.traccia.utils.FunctionalTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RunWith(VertxUnitRunner.class)
public class TracciaReStressTest extends FunctionalTestUtils {

    public final static String HOST = TracciaReFunctionalTest.HOST;
    public final static int PORT = TracciaReFunctionalTest.PORT;
    public final static String TEST_IMAGE = TracciaReFunctionalTest.TEST_IMAGE;

    final int REQUESTS_PER_THREAD = 50;
    final int THREADS = 50;

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
        List<String> createdTraces = sendSequentialRequests(REQUESTS_PER_THREAD);
        //Thread.sleep(50);  //wait for ALPR processing to happen
        for(String traceId: createdTraces){
            Assert.assertEquals(AppConstants.PROCESSED, getTraceStatus(traceId));
            deleteTrace(traceId);
        }
    }

    /**
     * @return List of all trace ids processed
     */
    protected List<String> sendSequentialRequests(int numberRequests) throws Exception {
        List<String> createdTraces = new ArrayList<>();
        for(int i=0; i < numberRequests; i++) {
            createdTraces.add(createTrace(buildTrace()));
            System.out.println("Done " + i);
        }
        return createdTraces;
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
                .forEach( createdTracesId -> {
                    Assert.assertEquals(REQUESTS_PER_THREAD, createdTracesId.size());
                    for(String traceId: createdTracesId){
                        Assert.assertEquals(AppConstants.PROCESSED, getTraceStatus(traceId));
                        deleteTrace(traceId);
                    }
                });
    }

}
