package re.traccia.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import re.traccia.common.AbstractRepository;
import re.traccia.management.AppConstants;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class ParkingSlotsRepository extends AbstractRepository {

    public ParkingSlotsRepository(MongoClient mongoClient) {
        setMongoClient(mongoClient);
        setCollection(AppConstants.PARKINGSLOTS);
    }

    public void findByPlatenumber(String plateNumber, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject jsonObject = new JsonObject().put("plateNumber", plateNumber);
        getMongoClient().findOne(AppConstants.PARKINGSLOTS, jsonObject, null, handler);
    }

    public void platenumberInCurrentSlot(String plateNumber, Handler<AsyncResult<Long>> handler) {
        Instant now = Instant.now();

        JsonObject plateNumberQuery = new JsonObject().put("plateNumber", plateNumber);
        JsonObject startDateLtNowQuery = new JsonObject().put("startDate", new JsonObject().put("$lt", new JsonObject().put("$date", now)));
        JsonObject endDateGtNowQuery = new JsonObject().put("endDate", new JsonObject().put("$gt", new JsonObject().put("$date", now)));
        JsonObject startAndEnd = new JsonObject().put("$and", new JsonArray(Arrays.asList(startDateLtNowQuery, endDateGtNowQuery)));
        JsonObject query = new JsonObject().put("$and", new JsonArray(Arrays.asList(plateNumberQuery, startAndEnd)));


        getMongoClient().count(AppConstants.PARKINGSLOTS, query, handler);
    }


}
