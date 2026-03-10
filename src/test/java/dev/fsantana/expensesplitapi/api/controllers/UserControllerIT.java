package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.requests.CreateUserRequest;
import dev.fsantana.expensesplitapi.api.requests.SignInRequest;
import dev.fsantana.expensesplitapi.configs.TestIntegrationConfig;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.security.models.Auth;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@TestPropertySource(locations = "classpath:application-test.yaml", properties = "api.security.token.expiration-time=1")
class UserControllerIT  extends TestIntegrationConfig  {

    @Test
    @DisplayName("should return 201 when create a user")
    void test0(){
        CreateUserRequest body = CreateUserRequest.builder()
                .name("John Doe")
                .email("johndoe@email.com")
                .password("1234567")
                .build();
        given()
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/users/sign-up")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is(body.getName()))
                .body("email", is(body.getEmail()))
                .body("token", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("should return 200 when create create a token")
    void test1() {
        User user = createUser();

        SignInRequest body = SignInRequest.builder()
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .build();
        given()
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/users/sign-in")
                .then()
                .statusCode(200)
                .body("id", is(user.getId().toString()))
                .body("name", is(user.getName()))
                .body("email", is(user.getEmail()))
                .body("token", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("should return 400 when create with user already exits")
    void test2()  {

        User user = createUser();

        CreateUserRequest body = CreateUserRequest.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .build();

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/users/sign-up")
                .then()
                .statusCode(400)
                .body("detail", is("Email/password invalid"));
    }

    @Test
    @DisplayName("should return 401 when login fails")
    void test3() {

        SignInRequest body = SignInRequest.builder()
                .email("invalid@email.com")
                .password("invalidpassword")
                .build();
        given()
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/users/sign-in")
                .then()
                .statusCode(401)
                .body("detail", is("Bad credentials"));
    }

    @Test
    @DisplayName("should return user data ")
    void test4() {
        Auth auth = token();

        given()
                .auth().oauth2(auth.getToken())
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("id", is(auth.getUser().getId().toString()))
                .body("name", is(auth.getUser().getName()))
                .body("email", is(auth.getUser().getEmail()));
    }

    @Test
    @DisplayName("should return user data ")
    void test5() {
        Auth auth = token();
        User userB = createUserB();
        given()
                .auth().oauth2(auth.getToken())
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("users.id", hasItems(userB.getId().toString()))
                .body("users.email", hasItems(userB.getEmail()))
                .body("users.name", hasItems(userB.getName()));
    }

    @Test
    @DisplayName("Should Returns all activities for a user.")
    public void test6() {
        Auth auth = this.token();
        User userB = createUserB();
        Activity example = createActivity(auth.getUser());
        createExpenseForActivity(auth.getUser().getId(), example, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .when()
                .get("/users/me/activities")
                .then()
                .statusCode(200)
                .body("activities.id", hasItems(example.getId().toString()))
                .body("activities.participantsAmount", hasItems(2))
                .body("activities.expensesAmount", hasItems(1))
                .body("activities.name", hasItems(example.getName()))
                .body("activities.totalAmountInCents", hasItems(1000))
                .body("activities.participants.email", hasItems(hasItems(auth.getUser().getEmail())));
    }

    @Test
    @DisplayName("should return statiscits")
    public void test7() {
        activityDataProvider.deleteAll();
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(auth.getUser().getId()));
        createExpensePayment(expense, 500L, auth.getUser());
        given()
                .auth().oauth2(auth.getToken())
                .get("/users/me/statistics")
                .then()
                .statusCode(200)
                .body("totalExpensesAmountInCents", is(1000))
                .body("amountPaidInCents", is(500))
                .body("expensesToPayCount", is(1))
                .body("amountToPayInCents", is(500))
                .body("paidExpensesCount", is(1))
                .body("activitiesCount", is(1))
                .body("expensesCount", is(1))
                .body("uniqueParticipantsCount", is(0));

    }

    @Test
    @DisplayName("should return statiscits")
    public void test8() {
        activityDataProvider.deleteAll();
        activityDataProvider.deleteAll();
        Auth auth = this.token();
        User userb = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userb));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L,
                List.of(auth.getUser().getId(), userb.getId()));
        createExpensePayment(expense, 500L, auth.getUser());
        given()
                .auth().oauth2(auth.getToken())
                .get("/users/me/statistics")
                .then()
                .statusCode(200)
                .body("totalExpensesAmountInCents", is(1000))
                .body("amountPaidInCents", is(500))
                .body("expensesToPayCount", is(0))
                .body("amountToPayInCents", is(0))
                .body("paidExpensesCount", is(1))
                .body("activitiesCount", is(1))
                .body("expensesCount", is(1))
                .body("uniqueParticipantsCount", is(1));

    }
}
