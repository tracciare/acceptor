package re.traccia.re.traccia.repository;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import re.traccia.model.Trace;
import re.traccia.repository.TracesRepository;
import re.traccia.verticle.MainVerticle;

import java.time.Instant;

@RunWith(VertxUnitRunner.class)
public class TraceRespositoryTest {

    Vertx vertx;
    MongoClient mongoClient;
    TracesRepository repo;

    @Before
    public void init() {
        vertx = Vertx.vertx();
        mongoClient = MongoClient.createNonShared(vertx, MainVerticle.mongoConfig());
        repo = new TracesRepository(mongoClient);
    }


    @Test
    public void saveTrace(TestContext context) {
        final Async async = context.async();

        Trace trace = new Trace("lat", "lon", null, "12345");
        trace.setStartDate(Instant.now());
        trace.setEndDate(Instant.now());

        repo.create(trace.toJson(), result -> {
            //repo.delete(result.result(), resultDel -> {});
            async.complete();
        });
    }

}
