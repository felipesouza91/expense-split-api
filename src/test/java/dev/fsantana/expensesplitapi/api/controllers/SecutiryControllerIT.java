package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.configs.TestIntegrationConfig;
import dev.fsantana.expensesplitapi.security.models.Auth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class SecutiryControllerIT extends TestIntegrationConfig {

    @Test
    @DisplayName("Should return 401 when token invalid")
    public void test0() {
        Auth auth = token();

        given()
                .auth().oauth2(auth.getToken()+"1232")
                .get("/balance/users/{userId}/detailed", auth.getUser().getId())
                .then()
                .statusCode(401)
                .body("detail", is("Token invalid"));
    }
}
