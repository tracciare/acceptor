package re.traccia;

import com.jayway.restassured.RestAssured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import re.traccia.management.AppConstants;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by fiorenzo on 28/05/16.
 */
public class MyRestIT {

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test
    public void getList() {
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


}
