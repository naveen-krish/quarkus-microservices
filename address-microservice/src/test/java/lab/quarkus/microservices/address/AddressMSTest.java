package lab.quarkus.microservices.address;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class AddressMSTest {

    @Test
    public void testGetAddress() {
        given()
          .when().get("/api/address")
          .then()
             .statusCode(200)
             .body("client_address",is("Beverly Hills USA"));
    }

}