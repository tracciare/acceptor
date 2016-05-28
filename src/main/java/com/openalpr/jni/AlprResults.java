package com.openalpr.jni;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AlprResults {
    private final long epoch_time;
    private final int img_width;
    private final int img_height;
    private final float total_processing_time_ms;

    private List<AlprPlateResult> plates;

    private List<AlprRegionOfInterest> regionsOfInterest;

    AlprResults(String json) {
        JsonObject jobj = new JsonObject(json);

        epoch_time = jobj.getLong("epoch_time");
        img_width = jobj.getInteger("img_width");
        img_height = jobj.getInteger("img_height");
        total_processing_time_ms = (float) jobj.getDouble("processing_time_ms").floatValue();

        JsonArray resultsArray = jobj.getJsonArray("results");
        plates = new ArrayList<AlprPlateResult>(resultsArray.size());
        for (int i = 0; i < resultsArray.size(); i++) {
            JsonObject plateObj = (JsonObject) resultsArray.getJsonObject(i);
            AlprPlateResult result = new AlprPlateResult(plateObj);
            plates.add(result);
        }

        JsonArray roisArray = jobj.getJsonArray("regions_of_interest");
        regionsOfInterest = new ArrayList<AlprRegionOfInterest>(roisArray.size());
        for (int i = 0; i < roisArray.size(); i++) {
            JsonObject roiObj = (JsonObject) roisArray.getJsonObject(i);
            AlprRegionOfInterest roi = new AlprRegionOfInterest(roiObj);
            regionsOfInterest.add(roi);
        }
    }

    public long getEpochTime() {
        return epoch_time;
    }

    public int getImgWidth() {
        return img_width;
    }

    public int getImgHeight() {
        return img_height;
    }

    public float getTotalProcessingTimeMs() {
        return total_processing_time_ms;
    }

    public List<AlprPlateResult> getPlates() {
        return plates;
    }

    public List<AlprRegionOfInterest> getRegionsOfInterest() {
        return regionsOfInterest;
    }
}
