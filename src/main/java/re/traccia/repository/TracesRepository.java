package re.traccia.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import re.traccia.common.AbstractRepository;
import re.traccia.management.AppConstants;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class TracesRepository extends AbstractRepository {

    public TracesRepository(MongoClient mongoClient) {
        setMongoClient(mongoClient);
        setCollection(AppConstants.TRACES);
    }

    public void image(String id, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        query.put("_id", id);
        getMongoClient().findOne(AppConstants.IMAGES, query, null, handler);
    }

    public void createImage(byte[] img, String id, Handler<AsyncResult<String>> handler) {
        JsonObject jsonObject = new JsonObject().put("_id", id).put("img", img);
        getMongoClient().insert(AppConstants.IMAGES, jsonObject, handler);
    }


}
