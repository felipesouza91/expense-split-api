package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.requests.ActivityRequest;
import dev.fsantana.expensesplitapi.api.requests.AddParticipantsRequest;
import dev.fsantana.expensesplitapi.configs.TestIntegrationConfig;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.security.models.Auth;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActivitiesControllerIT extends TestIntegrationConfig {

    @Test
    @DisplayName("Should create activity with success")
    public void teste0() {
        ActivityRequest body = Instancio.create(ActivityRequest.class);
        Auth auth = this.token();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is(body.getTitle()))
                .body("activityDate", notNullValue())
                .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("Should return 400 when value empty")
    public void teste1() {
        ActivityRequest body = Instancio.of(ActivityRequest.class)
                .setBlank(field(ActivityRequest::getTitle))
                .setBlank(field(ActivityRequest::getActivityDate))
                .create();
        Auth auth = this.token();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities")
                .then()
                .statusCode(400)
                .body("detail", is("Um ou mais campos estão invalidos. Faça o preenchimento correto e tente novamente"))
                .body("objects.name", hasItems("title", "activityDate"));
    }

    @Test
    @DisplayName("should return 404 when activity not found")
    public void test2(){
        Auth auth = this.token();
        given()

                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("should return activity details")
    public void test3(){
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{id}", activity.getId())
                .then()
                .statusCode(200)
                .body("name", is(activity.getName()))
                .body("participants.name", hasItems(activity.getParticipants().stream().findFirst().get().getUser().getName()))
                .body("id", is(activity.getId().toString()))
                .body("activityDate", notNullValue())
                .body("expenses.size()", is(0))
                .body("totalAmountInCents", is(0)) ;
    }

    @Test
    @DisplayName("should return 404 when activity not exists during update")
    public void test4(){
        Auth auth = this.token();
        ActivityRequest activityRequest = Instancio.create(ActivityRequest.class);
        given()
                .body(activityRequest)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/activities/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("Should return 400 when value empty during update")
    public void test5() {
        ActivityRequest body = Instancio.of(ActivityRequest.class)
                .setBlank(field(ActivityRequest::getTitle))
                .setBlank(field(ActivityRequest::getActivityDate))
                .create();
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());

        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/activities/{id}", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Um ou mais campos estão invalidos. Faça o preenchimento correto e tente novamente"))
                .body("objects.name", hasItems("title", "activityDate"));
    }

    @Test
    @DisplayName("Should return 200 when update success")
    public void test6() {
        ActivityRequest body = Instancio.create(ActivityRequest.class);
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSXXX");
        DateTimeFormatter dateTimeFormatter2 =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX");

        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/activities/{id}", activity.getId())
                .then()
                .statusCode(200)
                .body("id", is(activity.getId().toString()))
                .body("name", is(body.getTitle()))
                .body("activityDate", anyOf(is(dateTimeFormatter.format(body.getActivityDate())) , is(dateTimeFormatter2.format(body.getActivityDate())) ) )
                .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("should return 404 when activity not found during delete")
    public void test7() {
        Auth auth = this.token();
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/activities/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("should return 204 when delete with success")
    public void test8() {
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());

        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/activities/{id}", activity.getId())
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("should return 400 when body is invalid")
    public void test9() {
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        AddParticipantsRequest body = new AddParticipantsRequest();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities/{id}/participants", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Um ou mais campos estão invalidos. Faça o preenchimento correto e tente novamente"))
                .body("objects.name", hasItems("participantsIds"));;
    }

    @Test
    @DisplayName("should return 404 when activity not found")
    public void test10() {
        Auth auth = this.token();
        AddParticipantsRequest body = Instancio.create(AddParticipantsRequest.class);
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities/{id}/participants", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("should return 400 when participant id is not valid")
    public void test11() {
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        AddParticipantsRequest body = Instancio.create(AddParticipantsRequest.class);
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities/{id}/participants", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Participant not found"));
    }

    @Test
    @DisplayName("should return 201 when add participant to activity")
    public void test12() {
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        User userB = createUserB();
        AddParticipantsRequest body = Instancio.of(AddParticipantsRequest.class)
                .set(field(AddParticipantsRequest::getParticipantsIds), Set.of(userB.getId()))
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities/{id}/participants", activity.getId())
                .then()
                .statusCode(200)
                .body("activityId", is(activity.getId().toString()))
                .body("message", is("Participante adicionado com sucesso"))
                .body("addedParticipants.name", hasItems(userB.getName()))
                .body("addedParticipants.email", hasItems(userB.getEmail()))
                .body("addedParticipants.id", hasItems(userB.getId().toString()))
                .body("addedParticipants.joinedAt", hasItems(notNullValue()));
    }

    @Test
    @DisplayName("should return 201 when add participant to activity")
    public void test13() {
        Auth auth = this.token();
        Activity activity = createActivity(auth.getUser());
        User userB = createUserB();
        User userC = createUserC();
        AddParticipantsRequest body = Instancio.of(AddParticipantsRequest.class)
                .set(field(AddParticipantsRequest::getParticipantsIds), Set.of(userB.getId(), userC.getId(), auth.getUser().getId()))
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/activities/{id}/participants", activity.getId())
                .then()
                .statusCode(200)
                .body("activityId", is(activity.getId().toString()))
                .body("message", is(String.format("Adicionados %d participantes com sucesso", body.getParticipantsIds().size()-1)))
                .body("addedParticipants.name", hasItems(userB.getName()))
                .body("addedParticipants.email", hasItems(userB.getEmail()))
                .body("addedParticipants.id", hasItems(userB.getId().toString()))
                .body("addedParticipants.joinedAt", hasItems(notNullValue()));
    }

    @Test
    @DisplayName("should return 200 when get participant to activity")
    public void test14() {
        Auth auth = this.token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB, userC ));

        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{id}/participants", activity.getId())
                .then()
                .statusCode(200)
                .body("activityId", is(activity.getId().toString()))
                .body("activityName", is(activity.getName()))
                .body("participants.name", hasItems(userB.getName(), userC.getName(), auth.getUser().getName()))
                .body("participants.email", hasItems(userB.getEmail(), userC.getEmail(), auth.getUser().getEmail()))
                .body("participants.id", hasItems(userB.getId().toString(), userC.getId().toString(), auth.getUser().getId().toString()))
                .body("participants.joinedAt", hasItems(notNullValue()));
    }

    @Test
    @DisplayName("should return 404 when get participant to activity and activity not exists")
    public void test15() {
        Auth auth = this.token();

        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{id}/participants", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found or User not is a participant"));

    }

    @Test
    @DisplayName("should return 404 when delete participant to activity and participant not exists")
    public void test16() {
        Auth auth = this.token();

        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/activities/{id}/participants/{participantID}", UUID.randomUUID(), UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Participant not found"));

    }

    @Test
    @DisplayName("should return 400 when delete yourself participant to activity")
    public void test27() {
        Auth auth = this.token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB, userC ));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/activities/{id}/participants/{participantID}", activity.getId(), auth.getUser().getId())
                .then()
                .statusCode(400)
                .body("detail", is("Participant cannot  delete yourself"));

    }

    @Test
    @DisplayName("should return 404 when delete participant to activity and activity not exists")
    public void test17() {
        Auth auth = this.token();
        User userB = createUserB();
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/activities/{id}/participants/{participantID}", UUID.randomUUID(), userB.getId())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found or Session user not is a participant"));

    }


    @Test
    @DisplayName("should return 204 when delete participant to activity and activity exits")
    public void test18() {
        Auth auth = this.token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB, userC ));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/activities/{id}/participants/{participantID}", activity.getId(), userB.getId())
                .then()
                .statusCode(204);

        Optional<ActivityParticipant> byId = activityParticipantRepository.findByUserIdAndActivityId(userB.getId(), activity.getId());

        assertTrue(byId.isEmpty());

    }


    @Test
    @DisplayName("should return 404 when activity not exits")
    public void test19() {
        Auth auth = this.token();
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{activityId}/balance", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("Should 404 when activity not exists")
    public void test20() {
        Auth auth = this.token();
        given()
                .auth().oauth2(auth.getToken())
                .get("/activities/{activityId}/expenses", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("Should 400 when session user is not participant from activity")
    public void test22() {
        Auth auth = this.token();
        User userB = createUserB();
        Activity activity = createActivity(userB);
        given()
                .auth().oauth2(auth.getToken())
                .get("/activities/{activityId}/expenses", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Session user is not participant of this activity"));
    }

    @Test
    @DisplayName("should return 200 when activity is valid")
    public void test23() {
        Auth auth = this.token();
        User userB = createUserB();
        List<UUID> participantsIds = List.of(auth.getUser().getId(), userB.getId());
        Activity activity = createActivityWithParticipants(auth.getUser(),  Set.of(userB));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L,participantsIds );
        given()
                .auth().oauth2(auth.getToken())
                .get("/activities/{activityId}/expenses", activity.getId())
                .then()
                .statusCode(200)
                .body("expenses.payer.userId", hasItems(auth.getUser().getId().toString()))
                .body("expenses.payer.name", hasItems(auth.getUser().getName()))
                .body("expenses.createdAt", hasItems(notNullValue()))
                .body("expenses.id", hasItems(expense.getId().toString()))
                .body("expenses.name", hasItems(expense.getName()))
                .body("expenses.amountInCents", hasItem(expense.getAmountInCents().intValue()))
                .body("expenses.participantsCount", hasItems(participantsIds.size()));
    }

    @Test
    @DisplayName("should return 200 with activity balance")
    public void test24() {
        Auth auth = this.token();
        User userB = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{activityId}/balance", activity.getId())
                .then()
                .statusCode(200)
                .body("activityName", is(activity.getName()))
                .body("activityId", is(activity.getId().toString()))
                .body("transfers.from.userId", hasItems(userB.getId().toString()))
                .body("transfers.from.name", hasItems(userB.getName()))
                .body("transfers.amountInCents", hasItems(1000))
                .body("transfers.to.userId", hasItems(auth.getUser().getId().toString()))
                .body("transfers.to.name", hasItems(auth.getUser().getName()));
    }

    @Test
    @DisplayName("should return 200 with activity balance")
    public void test25() {
        Auth auth = this.token();
        User userB = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        createExpenseForActivity(null, activity, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{activityId}/balance", activity.getId())
                .then()
                .statusCode(200)
                .body("activityName", is(activity.getName()))
                .body("activityId", is(activity.getId().toString()))
                .body("transfers.size()", is(0));
    }

    @Test
    @DisplayName("should return 200 with activity balance")
    public void test26() {
        Auth auth = this.token();
        User userB = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        createExpenseForActivity(userB.getId() , activity, 1000L, List.of(auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/activities/{activityId}/balance", activity.getId())
                .then()
                .statusCode(200)
                .body("activityName", is(activity.getName()))
                .body("activityId", is(activity.getId().toString()))
                .body("transfers.from.userId", hasItems(auth.getUser().getId().toString()))
                .body("transfers.from.name", hasItems(auth.getUser().getName()))
                .body("transfers.amountInCents", hasItems(1000))
                .body("transfers.to.userId", hasItems(userB.getId().toString()))
                .body("transfers.to.name", hasItems(userB.getName()));
    }
}
