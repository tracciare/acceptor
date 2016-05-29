package re.traccia.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class Trace {
    private String lat;
    private String lon;
    private byte[] image;
    private String plateNumber;
    private Instant startDate;
    private Instant endDate;
    private String status;
    private JsonObject alpr;

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

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonObject getAlpr() {
        return alpr;
    }

    public void setAlpr(JsonObject alpr) {
        this.alpr = alpr;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
//        jsonObject
//                .put("lat", this.getLat())
//                .put("lon", this.getLon());
        if (this.getStartDate() != null)
            jsonObject.put("startDate", new JsonObject().put("$date", this.getStartDate()));
        if (this.getEndDate() != null)
            jsonObject.put("endDate", new JsonObject().put("$date", this.getEndDate()));
        if (lat != null && lon != null)
            jsonObject
                    .put("location", new JsonObject()
                            .put("type", "Point")
                            .put("coordinates", new JsonArray().add(new Double(lat)).add(new Double(lon))
                            )
                    );
//        location : { type: "Point", coordinates: [ -73.97, 40.77 ] },
        jsonObject
                .put("status", this.getStatus())
                .put("plateNumber", this.getPlateNumber())
                .put("alpr", this.getAlpr());
        return jsonObject;
    }
}
