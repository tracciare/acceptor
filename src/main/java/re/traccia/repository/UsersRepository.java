package re.traccia.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.mongo.MongoClient;
import re.traccia.common.AbstractRepository;
import re.traccia.management.AppConstants;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class UsersRepository extends AbstractRepository {

    public UsersRepository(MongoClient mongoClient) {
        setMongoClient(mongoClient);
        setCollection(AppConstants.USERS);
    }

    public void findByPlatenumber(String plateNumber, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject jsonObject = new JsonObject().put("plateNumber", plateNumber);
        getMongoClient().findOne(AppConstants.USERS, jsonObject, null, handler);
    }


}
