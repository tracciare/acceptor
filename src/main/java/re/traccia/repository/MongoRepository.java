package re.traccia.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
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
        query.put("_id", id);
        mongoClient.findOne("traces", query, null, handler);
    }

    public void create(Trace trace, Handler<AsyncResult<String>> handler) {
        JsonObject jsonObject = TraceUtils.toJson(trace);
        mongoClient.insert("traces", jsonObject, handler);
    }


    public void update(String id, Trace trace, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("id", id);
        JsonObject updateQuery = new JsonObject();
        updateQuery.put("$set", TraceUtils.toJson(trace));
        mongoClient.update("traces", query, updateQuery, handler);
    }

    public void delete(String id, Handler<AsyncResult<Void>> handler) {
        JsonObject query = new JsonObject();
        query.put("id", id);
        mongoClient.removeOne("traces", query, handler);
    }


    public void userByLoginAndPwd(String login, String hashedPwd, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        query.put("login", login);
        query.put("password", hashedPwd);
        mongoClient.findOne("users", query, null, handler);
    }


    public void getFeed(String feedHash, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject query = new JsonObject();
        query.put("hash", feedHash);
        mongoClient.findOne("feeds", query, null, handler);
    }


    @SuppressWarnings("unchecked")
    public void unsubscribe(JsonObject user, JsonObject subscription, Handler<AsyncResult<?>> handler) {
        List<JsonObject> subscriptions = user.getJsonArray("subscriptions").getList();
        subscriptions.removeIf(sub -> {
            return sub.getString("hash").equals(subscription.getString("hash"));
        });
        JsonObject newSubscriptions = new JsonObject();
        newSubscriptions.put("$set", new JsonObject().put("subscriptions", new JsonArray(subscriptions)));
        JsonObject userQuery = new JsonObject();
        userQuery.put("_id", user.getString("_id"));
        mongoClient.update("users", userQuery, newSubscriptions, updateHandler -> {
            if (updateHandler.failed()) {
                handler.handle(updateHandler);
                return;
            }
            JsonObject feedQuery = new JsonObject();
            feedQuery.put("_id", subscription.getString("_id"));
            mongoClient.findOne("feeds", feedQuery, null, duplicateHandler -> {
                if (duplicateHandler.failed()) {
                    handler.handle(duplicateHandler);
                    return;
                }
                JsonObject feed = duplicateHandler.result();
                JsonObject updateQuery = new JsonObject();
                Integer oldCount = feed.getInteger("subscriber_count", 1);
                subscription.put("subscriber_count", oldCount - 1);
                updateQuery.put("_id", feed.getString("_id"));
                JsonObject updateValue = new JsonObject();
                updateValue.put("$set", subscription);
                mongoClient.update("feeds", updateQuery, updateValue, feedUpdateHandler -> {
                    handler.handle(feedUpdateHandler);
                });
            });

        });
    }

    public void newSubscription(JsonObject user, JsonObject subscription, Handler<AsyncResult<?>> handler) {
        String urlHash = subscription.getString("hash");
        JsonObject findQuery = new JsonObject();
        findQuery.put("hash", urlHash);
        mongoClient.findOne("feeds", findQuery, null, findResult -> {
            if (findResult.failed()) {
                handler.handle(findResult);
                return;
            }
            JsonObject existingFeed = findResult.result();
            if (existingFeed == null) {
                subscription.put("subscriber_count", 1);
                mongoClient.insert("feeds", subscription, insertResult -> {
                    if (insertResult.failed()) {
                        handler.handle(insertResult);
                        return;
                    }
                    subscription.put("_id", insertResult.result());
                    attachSubscriptionToUser(user, subscription, handler);
                });
            } else {
                JsonObject updateQuery = new JsonObject();
                Integer oldCount = existingFeed.getInteger("subscriber_count", 0);
                subscription.put("subscriber_count", oldCount + 1);
                updateQuery.put("_id", existingFeed.getString("_id"));
                subscription.put("_id", existingFeed.getString("_id"));
                JsonObject updateValue = new JsonObject();
                updateValue.put("$set", subscription);
                mongoClient.update("feeds", updateQuery, updateValue, updateHandler -> {
                    if (updateHandler.failed()) {
                        handler.handle(updateHandler);
                        return;
                    }
                    attachSubscriptionToUser(user, subscription, handler);
                });
            }
        });
    }

    private void attachSubscriptionToUser(JsonObject user, JsonObject subscription, Handler<AsyncResult<?>> handler) {
        JsonArray subscriptions = user.getJsonArray("subscriptions", new JsonArray());
        subscriptions.add(subscription);
        JsonObject query = new JsonObject();
        query.put("_id", user.getString("_id"));
        JsonObject newSubscriptions = new JsonObject();
        newSubscriptions.put("$set", new JsonObject().put("subscriptions", subscriptions));
        mongoClient.update("users", query, newSubscriptions, res -> {
            handler.handle(res);
        });
    }

}
