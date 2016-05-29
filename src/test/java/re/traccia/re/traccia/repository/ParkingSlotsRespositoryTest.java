package re.traccia.re.traccia.repository;

import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import re.traccia.model.ParkingSlot;
import re.traccia.model.Trace;
import re.traccia.repository.ParkingSlotsRepository;
import re.traccia.repository.TracesRepository;
import re.traccia.verticle.MainVerticle;

import java.time.Instant;

@RunWith(VertxUnitRunner.class)
public class ParkingSlotsRespositoryTest {

    Vertx vertx;
    MongoClient mongoClient;
    ParkingSlotsRepository repo;

    @Before
    public void init() {
        vertx = Vertx.vertx();
        mongoClient = MongoClient.createNonShared(vertx, MainVerticle.mongoConfig());
        repo = new ParkingSlotsRepository(mongoClient);
    }


    @Test
    public void saveTrace(TestContext context) {
        final Async async = context.async();

        ParkingSlot ps = new ParkingSlot("1234", Instant.now().minusSeconds(60), Instant.now().plusSeconds(60), "4567");

        repo.create(ps.toJson(), resultCreate -> {
            if(resultCreate.succeeded()) {
                repo.platenumberInCurrentSlot("1234", resultCount -> {
                    if (resultCount.succeeded()) {
                        Assert.assertEquals(new Long(1), resultCount.result());
                    } else {
                        Assert.fail();
                    }
                });
                repo.delete(resultCreate.result(), resultDel -> {});
            } else {
                Assert.fail();
            }
            async.complete();
        });
    }

}
