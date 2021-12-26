package lab.quarkus.microservices.anagrafe;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class AnagrafeMSTest {

    @Test
    public void testGetCustomers() {
        given()
          .when().get("/api/customers")
          .then()
             .statusCode(200);
             //.body(is("client_address"));
    }

}