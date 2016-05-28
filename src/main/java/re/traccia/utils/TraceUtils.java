package re.traccia.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import re.traccia.model.Trace;

import java.util.List;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class TraceUtils {

    public static JsonObject toJson(Trace trace) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("id", trace.getId())
                .put("lat", trace.getLat())
                .put("lon", trace.getLon())
                .put("image", trace.getImage())
                .put("plateNumber", trace.getPlateNumber());
        return jsonObject;
    }

    public JsonArray toJsonArray(List<Trace> traceList) {
        JsonArray jsonArray = new JsonArray();
        for (Trace trace : traceList) {
            jsonArray.add(toJson(trace));
        }
        return jsonArray;
    }
}
