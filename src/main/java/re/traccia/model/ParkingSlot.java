package re.traccia.model;

import io.vertx.core.json.JsonObject;

import java.time.Instant;

/**
 * Created by fiorenzo on 29/05/16.
 */
public class ParkingSlot {

    private String plateNumber;
    private Instant startDate;
    private Instant endDate;
    private String userId;

    public ParkingSlot(String plateNumber, String userId, Instant startDate) {
        this.plateNumber = plateNumber;
        this.userId = userId;
        this.startDate = startDate;
    }

    public ParkingSlot(String plateNumber, Instant startDate, Instant endDate, String userId) {
        this.plateNumber = plateNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    public ParkingSlot() {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject
                .put("plateNumber", this.getPlateNumber());
        if (this.getStartDate() != null)
            jsonObject.put("startDate", new JsonObject().put("$date", this.getStartDate()));
        if (this.getEndDate() != null)
            jsonObject.put("endDate", new JsonObject().put("$date", this.getEndDate()));
        jsonObject
                .put("userId", this.getUserId());
        return jsonObject;
    }
}
