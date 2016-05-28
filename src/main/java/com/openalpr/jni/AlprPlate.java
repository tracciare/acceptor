package com.openalpr.jni;


import io.vertx.core.json.JsonObject;

public class AlprPlate {
    private final String characters;
    private final float overall_confidence;
    private final boolean matches_template;

    AlprPlate(JsonObject plateObj) {
        characters = plateObj.getString("plate");
        overall_confidence = plateObj.getDouble("confidence").floatValue();
        matches_template = plateObj.getInteger("matches_template") != 0;
    }

    public String getCharacters() {
        return characters;
    }

    public float getOverallConfidence() {
        return overall_confidence;
    }

    public boolean isMatchesTemplate() {
        return matches_template;
    }
}
