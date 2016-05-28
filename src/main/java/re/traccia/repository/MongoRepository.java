package re.traccia.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;
import re.traccia.utils.TraceUtils;

import java.util.List;

public class MongoRepository {

    private MongoClient mongoClient;


    public MongoRepository(MongoClient mongo) {
        this.mongoClient = mongo;
    }

    public void fetch(String id, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        JsonObject fields = new JsonObject().put("id", 1).put("lat", 1).put("lon", 1).put("plateNumber", 1);
        query.put("_id", id);
        mongoClient.findOne(AppConstants.TRACES, query, fields, handler);
    }

    public void getImg(String id, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        query.put("_id", id);
        mongoClient.findOne(AppConstants.IMAGES, query, null, handler);
    }

    public void create(JsonObject jsonObject, Handler<AsyncResult<String>> handler) {
        mongoClient.insert(AppConstants.TRACES, jsonObject, handler);
    }

    public void createImg(byte[] img, String id, Handler<AsyncResult<String>> handler) {
        JsonObject jsonObject = new JsonObject().put("_id", id).put("img", img);
        mongoClient.insert(AppConstants.IMAGES, jsonObject, handler);
    }


    public void update(String id, JsonObject jsonObject, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("id", id);
        JsonObject updateQuery = new JsonObject();
        updateQuery.put("$set", jsonObject);
        mongoClient.update(AppConstants.TRACES, query, updateQuery, handler);
    }

    public void delete(String id, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("id", id);
        mongoClient.removeOne(AppConstants.TRACES, query, handler);
    }

    public void list(JsonObject query, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        mongoClient.find(AppConstants.TRACES, query, resultHandler);
    }

}
