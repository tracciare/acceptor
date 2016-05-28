package re.traccia.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class AbstractRepository implements Repository {

    private String collection;
    private MongoClient mongoClient;


    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void fetch(String id, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        query.put("_id", id);
        mongoClient.findOne(getCollection(), query, null, handler);
    }

    public void create(JsonObject jsonObject, Handler<AsyncResult<String>> handler) {
        mongoClient.insert(getCollection(), jsonObject, handler);
    }


    public void update(String id, JsonObject jsonObject, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("id", id);
        JsonObject updateQuery = new JsonObject();
        updateQuery.put("$set", jsonObject);
        mongoClient.update(getCollection(), query, updateQuery, handler);
    }

    public void delete(String id, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("id", id);
        mongoClient.removeOne(getCollection(), query, handler);
    }

    public void list(JsonObject query, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        mongoClient.find(getCollection(), query, resultHandler);
    }
}

