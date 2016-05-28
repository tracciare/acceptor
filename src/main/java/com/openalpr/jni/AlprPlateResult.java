package com.openalpr.jni;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AlprPlateResult {
    // The number requested is always >= the topNPlates count
    private final int requested_topn;

    // the best plate is the topNPlate with the highest confidence
    private final AlprPlate bestPlate;

    // A list of possible plate number permutations
    private List<AlprPlate> topNPlates;

    // The processing time for this plate
    private final float processing_time_ms;

    // the X/Y coordinates of the corners of the plate (clock-wise from top-left)
    private List<AlprCoordinate> plate_points;

    // The index of the plate if there were multiple plates returned
    private final int plate_index;

    // When region detection is enabled, this returns the region.  Region detection is experimental
    private final int regionConfidence;
    private final String region;

    AlprPlateResult(JsonObject plateResult) {
        requested_topn = plateResult.getInteger("requested_topn");

        JsonArray candidatesArray = plateResult.getJsonArray("candidates");

        if (candidatesArray.size() > 0)
            bestPlate = new AlprPlate((JsonObject) candidatesArray.getJsonObject(0));
        else
            bestPlate = null;

        topNPlates = new ArrayList<AlprPlate>(candidatesArray.size());
        for (int i = 0; i < candidatesArray.size(); i++) {
            JsonObject candidateObj = (JsonObject) candidatesArray.getJsonObject(i);
            AlprPlate newPlate = new AlprPlate(candidateObj);
            topNPlates.add(newPlate);
        }

        JsonArray coordinatesArray = plateResult.getJsonArray("coordinates");
        plate_points = new ArrayList<AlprCoordinate>(coordinatesArray.size());
        for (int i = 0; i < coordinatesArray.size(); i++) {
            JsonObject coordinateObj = (JsonObject) coordinatesArray.getJsonObject(i);
            AlprCoordinate coordinate = new AlprCoordinate(coordinateObj);
            plate_points.add(coordinate);
        }

        processing_time_ms = (float) plateResult.getDouble("processing_time_ms").floatValue();
        plate_index = plateResult.getInteger("plate_index");

        regionConfidence = plateResult.getInteger("region_confidence");
        region = plateResult.getString("region");

    }

    public int getRequestedTopn() {
        return requested_topn;
    }

    public AlprPlate getBestPlate() {
        return bestPlate;
    }

    public List<AlprPlate> getTopNPlates() {
        return topNPlates;
    }

    public float getProcessingTimeMs() {
        return processing_time_ms;
    }

    public List<AlprCoordinate> getPlatePoints() {
        return plate_points;
    }

    public int getPlateIndex() {
        return plate_index;
    }

    public int getRegionConfidence() {
        return regionConfidence;
    }

    public String getRegion() {
        return region;
    }
}
