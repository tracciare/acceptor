package com.openalpr.jni;


import io.vertx.core.json.JsonObject;

public class AlprRegionOfInterest {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    AlprRegionOfInterest(JsonObject roiObj) {
        x = roiObj.getInteger("x");
        y = roiObj.getInteger("y");
        width = roiObj.getInteger("width");
        height = roiObj.getInteger("height");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
