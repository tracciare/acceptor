package re.traccia.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

/**
 * Created by fiorenzo on 28/05/16.
 */
public interface Repository {

     String getCollection();
     void setCollection(String collection);

     MongoClient getMongoClient();
     void setMongoClient(MongoClient mongoClient);

     void fetch(String id, Handler<AsyncResult<JsonObject>> handler);
     void create(JsonObject jsonObject, Handler<AsyncResult<String>> handler);
     void update(String id, JsonObject jsonObject, Handler<AsyncResult<Void>> handler);
     void delete(String id, Handler<AsyncResult<Void>> handler);
     void list(JsonObject query, Handler<AsyncResult<List<JsonObject>>> resultHandler);


}
