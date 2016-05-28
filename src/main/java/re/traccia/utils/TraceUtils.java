package re.traccia.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import re.traccia.model.Trace;

import java.util.List;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class TraceUtils {

    public JsonArray toJsonArray(List<Trace> traceList) {
        JsonArray jsonArray = new JsonArray();
        for (Trace trace : traceList) {
            jsonArray.add(trace.toJson());
        }
        return jsonArray;
    }
}
