package re.traccia.repository;

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

}
