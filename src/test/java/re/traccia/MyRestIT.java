package re.traccia;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.vertx.core.json.Json;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import re.traccia.management.AppConstants;
import re.traccia.model.Trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.parsing.Parser.JSON;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class MyRestIT {

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://192.168.99.100";
        RestAssured.port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test
    public void getListTest() {
        // Get the list of bottles, ensure it's a success and extract the first id.
        final int id = get(AppConstants.TRACES_PATH).then()
                .assertThat()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("find { it.name=='Bowmore 15 Years Laimrig' }.id");
        // Now get the individual resource and check the content
        get(AppConstants.TRACES_PATH + id).then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Bowmore 15 Years Laimrig"))
                .body("origin", equalTo("Scotland, Islay"))
                .body("id", equalTo(id));
    }


    @Test
    public void postTest() throws IOException {
        Path path = Paths.get("docs/auto.jpg");
        byte[] data = Files.readAllBytes(path);
        Trace trace = new Trace("lat", "lon", data, null);
        given()
                .contentType(ContentType.JSON)
                .body(Json.encode(trace))
                .when()
                .post(AppConstants.TRACES_PATH)
                .then()
                .assertThat()
                .statusCode(200);
    }


}
