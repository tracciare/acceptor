package re.traccia.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class Trace {
    private String lat;
    private String lon;
    private byte[] image;
    private String plateNumber;

    public Trace() {
    }

    public Trace(String lat, String lon, byte[] image, String plateNumber) {
        this.lat = lat;
        this.lon = lon;
        this.image = image;
        this.plateNumber = plateNumber;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject
                .put("lat", this.getLat())
                .put("lon", this.getLon())
                .put("plateNumber", this.getPlateNumber());
        return jsonObject;
    }
}
