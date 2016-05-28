package com.openalpr.jni;


import io.vertx.core.json.JsonObject;

public class AlprCoordinate {
    private final int x;
    private final int y;

    AlprCoordinate(JsonObject coordinateObj) {
        x = coordinateObj.getInteger("x");
        y = coordinateObj.getInteger("y");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
