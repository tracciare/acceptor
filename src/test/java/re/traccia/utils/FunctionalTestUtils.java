package re.traccia.utils;

import com.jayway.restassured.http.ContentType;
import io.vertx.core.json.Json;
import org.junit.Assert;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;

public class FunctionalTestUtils {

    public static String createTrace(Trace trace) throws IOException {
        return given().
                contentType(ContentType.JSON).
                body(Json.encode(trace)).
                when().post(AppConstants.TRACES_PATH).
                then().assertThat()
                .statusCode(200).
                        extract()
                .path("_id");
    }

    /*
    public static void processAlpr(String newTraceId) {
        List results = given().
                when().get(AppConstants.ALPR_PATH + newTraceId).
                then().assertThat()
                .statusCode(200).
                        extract()
                .body().jsonPath().getList("results");

        Assert.assertTrue(results.size() > 0);
    }
    */

    public static String getTraceStatus(String traceId) {
        String status = given().
                when().get(AppConstants.TRACES_PATH + traceId).
                then().assertThat()
                .statusCode(200).
                        extract()
                .path("status");

        Assert.assertNotNull(status);
        Assert.assertFalse(status.isEmpty());

        return status;
    }

    public static void deleteTrace(String newTraceId) {
        given().
                when().
                delete(AppConstants.TRACES_PATH + newTraceId).
                then().assertThat().
                statusCode(200);
    }

}
