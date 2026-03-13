package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.requests.CreateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.MarkPaymentRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdatePayerExpenseRequest;
import dev.fsantana.expensesplitapi.configs.TestIntegrationConfig;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.security.models.Auth;
import io.restassured.http.ContentType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpensesControllerIT extends TestIntegrationConfig {

    @Test
    @DisplayName("Should return 400 if body is invalid")
    public void test0() {
        Auth auth = token();
        CreateExpenseRequest body = new CreateExpenseRequest();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", UUID.randomUUID())
                .then()
                .statusCode(400)
                .body("detail", is("Um ou mais campos estão invalidos. Faça o preenchimento correto e tente novamente"))
                .body("objects.name", hasItems("title", "amountInCents", "participantsIds"));
    }

    @Test
    @DisplayName("Should return 400 if body activity nor found")
    public void test1() {
        Auth auth = token();
        CreateExpenseRequest body = Instancio.create(CreateExpenseRequest.class);
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", UUID.randomUUID())
                .then()
                .statusCode(400)
                .body("detail", is("Activity not found"));
    }

    @Test
    @DisplayName("Should return 400 if session user isn`t a activity participant")
    public void test2() {
        Auth auth = token();
        Activity activity = createActivity(createUserB());
        CreateExpenseRequest body = Instancio.of(CreateExpenseRequest.class)
                .setBlank(field(CreateExpenseRequest::getPayerId))
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("User is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 400 if  payer id is invalid")
    public void test3() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        CreateExpenseRequest body = Instancio.create(CreateExpenseRequest.class);
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Payer not found"));
    }

    @Test
    @DisplayName("Should return 400 if participant ids in request are invalid")
    public void test4() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        User userc = createUserC();
        CreateExpenseRequest body = Instancio.of(CreateExpenseRequest.class)
                .set(field(CreateExpenseRequest::getPayerId), userc.getId())
                .setBlank(field(CreateExpenseRequest::getPayerId))
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", activity.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Participant not found"));
    }

    @Test
    @DisplayName("Should return 201 when save with one participant")
    public void test5() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        User user = createUserB();
        CreateExpenseRequest body = Instancio.of(CreateExpenseRequest.class)
                .setBlank(field(CreateExpenseRequest::getPayerId))
                .set(field(CreateExpenseRequest::getParticipantsIds), Set.of(user.getId()))
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", activity.getId())
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("activityId", is(activity.getId().toString()))
                .body("name", is(body.getTitle()))
                .body("amountInCents", is(body.getAmountInCents()))
                .body("participants.userId", hasItems(user.getId().toString()))
                .body("participants.userName", hasItems(user.getName()))
                .body("participants.amountOwedInCents", hasItems(body.getAmountInCents()))
                .body("createdAt",notNullValue());
    }

    @Test
    @DisplayName("Should return 201 with save with two participant")
    public void test6() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        User user = createUserB();
        User userC = createUserC();
        Set<UUID> participantsIds = Set.of(user.getId(), userC.getId());
        CreateExpenseRequest body = Instancio.of(CreateExpenseRequest.class)
                .setBlank(field(CreateExpenseRequest::getPayerId))
                .set(field(CreateExpenseRequest::getParticipantsIds), participantsIds)
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", activity.getId())
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("activityId", is(activity.getId().toString()))
                .body("name", is(body.getTitle()))
                .body("amountInCents", is(body.getAmountInCents()))
                .body("participants.userId", hasItems(user.getId().toString(), userC.getId().toString()))
                .body("participants.userName", hasItems(user.getName() , userC.getName()))
                .body("participants.amountOwedInCents", hasItems(body.getAmountInCents()/participantsIds.size(), body.getAmountInCents()/participantsIds.size()))
                .body("createdAt",notNullValue());
    }

    @Test
    @DisplayName("Should return 201 with payerId save with two participant")
    public void test7() {
        Auth auth = token();
        User user = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(user));

        User userC = createUserC();
        Set<UUID> participantsIds = Set.of(user.getId(), userC.getId());
        CreateExpenseRequest body = Instancio.of(CreateExpenseRequest.class)
                .set(field(CreateExpenseRequest::getPayerId), auth.getUser().getId())
                .set(field(CreateExpenseRequest::getParticipantsIds), participantsIds)
                .create();
        given()
                .body(body)
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}", activity.getId())
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("activityId", is(activity.getId().toString()))
                .body("name", is(body.getTitle()))
                .body("payerId", is(auth.getUser().getId().toString()))
                .body("payerName", is(auth.getUser().getName()))
                .body("amountInCents", is(body.getAmountInCents()))
                .body("participants.userId", hasItems(user.getId().toString(), userC.getId().toString()))
                .body("participants.userName", hasItems(user.getName() , userC.getName()))
                .body("participants.amountOwedInCents", hasItems(body.getAmountInCents()/participantsIds.size(), body.getAmountInCents()/participantsIds.size()))
                .body("createdAt",notNullValue());
    }

    @Test
    @DisplayName("Should return 404 if expense not found")
    public void test8() {
        Auth auth = token();
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/expenses/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Expense not found"));
    }

    @Test
    @DisplayName("Should return 400 if session user is not a expense participant")
    public void test9() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of( userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/expenses/{id}", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("User is not participant of this expense"));
    }

    @Test
    @DisplayName("Should return 200 on get")
    public void test10() {
        Auth auth = token();
        User payer = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of( auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/expenses/{id}", expense.getId())
                .then()
                .statusCode(200)
                .body("payer.userId", is(payer.getId().toString()))
                .body("payer.name", is(payer.getName()))
                .body("payer.email", is(payer.getEmail()))
                .body("activityName", is(activity.getName()))
                .body("createdAt", notNullValue())
                .body("amountInCents", is(1000))
                .body("activityId", is(activity.getId().toString()))
                .body("name", is(expense.getName()))
                .body("id", is(expense.getId().toString()))
                .body("payments.size()", is(0) )
                .body("participants.amountPaidInCents", hasItems(0))
                .body("participants.amountOwedInCents", hasItems(1000))
                .body("participants.remainingDebtInCents", hasItems(1000))
                .body("participants.paymentStatus", hasItems("pending"))
                .body("participants.name", hasItems(auth.getUser().getName()))
                .body("participants.userId", hasItems(auth.getUser().getId().toString()))
                .body("participants.email", hasItems(auth.getUser().getEmail()));
    }

    @Test
    @DisplayName("Should return 200 in expense with two participants on get")
    public void test11() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC));
        List<UUID> participantsIds = List.of(auth.getUser().getId(), userC.getId());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, participantsIds);
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .get("/expenses/{id}", expense.getId())
                .then()
                .statusCode(200)
                .body("payer.userId", is(payer.getId().toString()))
                .body("payer.name", is(payer.getName()))
                .body("payer.email", is(payer.getEmail()))
                .body("activityName", is(activity.getName()))
                .body("createdAt", notNullValue())
                .body("amountInCents", is(1000))
                .body("activityId", is(activity.getId().toString()))
                .body("name", is(expense.getName()))
                .body("id", is(expense.getId().toString()))
                .body("payments.size()", is(0) )
                .body("participants.amountPaidInCents", hasItems(0))
                .body("participants.amountOwedInCents", hasItems(1000/2, 1000/2))
                .body("participants.remainingDebtInCents", hasItems(1000/2, 1000/2))
                .body("participants.paymentStatus", hasItems("pending","pending"))
                .body("participants.name", hasItems(auth.getUser().getName(), userC.getName()))
                .body("participants.userId", hasItems(auth.getUser().getId().toString(), userC.getId().toString()))
                .body("participants.email", hasItems(auth.getUser().getEmail(), userC.getEmail()));
    }

    @Test
    @DisplayName("Should return 404 when expense not exits on update expense")
    public void test12() {
        Auth auth = token();
        UpdateExpenseRequest body = new UpdateExpenseRequest();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Expense not found"));
    }

    @Test
    @DisplayName("Should return 400 when session user is not a participant on update expense")
    public void test13() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(userC);
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of( payer.getId()));
        UpdateExpenseRequest body = new UpdateExpenseRequest();
        body.setParticipantsIds(Set.of(userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("User is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 200 when update participant ")
    public void test14() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC));
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(payer.getId()));
        UpdateExpenseRequest body = Instancio.of(UpdateExpenseRequest.class)
                .setBlank(field(UpdateExpenseRequest::getPayerId))
                .setBlank(field(UpdateExpenseRequest::getAmountInCents))
                .setBlank(field(UpdateExpenseRequest::getTitle))
                .set(field(UpdateExpenseRequest::getParticipantsIds), Set.of(userC.getId()))
                .create();

        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", expense.getId())
                .then()
                .statusCode(200)
                .body("activityId", is(activity.getId().toString()))
                .body("amountInCents", is(expense.getAmountInCents().intValue()))
                .body("id", is(expense.getId().toString()))
                .body("name", is(expense.getName()))
                .body("createdAt", notNullValue())
                .body("participants.userId", hasItems(payer.getId().toString(), userC.getId().toString()))
                .body("participants.userName", hasItems(payer.getName(), userC.getName()))
                .body("participants.amountOwedInCents", hasItems(expense.getAmountInCents().intValue()/2, expense.getAmountInCents().intValue()/2))
                .body("payerName", is(payer.getName()));
    }

    @Test
    @DisplayName("Should return 200 when update participant with same amount")
    public void test44() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC, payer));
        Expense expense = createExpenseForActivity(
                auth.getUser().getId() , activity, 1000L, List.of(userC.getId()));
        UpdateExpenseRequest body = Instancio.of(UpdateExpenseRequest.class)
                .set(field(UpdateExpenseRequest::getPayerId), payer.getId())
                .set(field(UpdateExpenseRequest::getAmountInCents), 1000)
                .setBlank(field(UpdateExpenseRequest::getTitle))
                .setBlank(field(UpdateExpenseRequest::getParticipantsIds))
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", expense.getId())
                .then()
                .statusCode(200)
                .body("activityId", is(activity.getId().toString()))
                .body("amountInCents", is(expense.getAmountInCents().intValue()))
                .body("id", is(expense.getId().toString()))
                .body("name", is(expense.getName()))
                .body("createdAt", notNullValue())
                .body("participants.userId", hasItems( userC.getId().toString()))
                .body("participants.userName", hasItems( userC.getName()))
                .body("participants.amountOwedInCents", hasItems( expense.getAmountInCents().intValue()))
                .body("payerName", is(payer.getName()));
    }

    @Test
    @DisplayName("Should return 400 when payer is not a participant on update ")
    public void test45() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC));
        Expense expense = createExpenseForActivity(
                auth.getUser().getId() , activity, 1000L, List.of(userC.getId()));
        UpdateExpenseRequest body = Instancio.of(UpdateExpenseRequest.class)
                .set(field(UpdateExpenseRequest::getPayerId), payer.getId())
                .set(field(UpdateExpenseRequest::getAmountInCents), 1000)
                .setBlank(field(UpdateExpenseRequest::getTitle))
                .setBlank(field(UpdateExpenseRequest::getParticipantsIds))
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Payer is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 400 when empty participant on update ")
    public void test46() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC));
        Expense expense = createExpenseForActivity(
                auth.getUser().getId() , activity, 1000L, Collections.EMPTY_LIST);
        UpdateExpenseRequest body = Instancio.of(UpdateExpenseRequest.class)
                .set(field(UpdateExpenseRequest::getPayerId), auth.getUser().getId())
                .set(field(UpdateExpenseRequest::getAmountInCents), 2000)
                .setBlank(field(UpdateExpenseRequest::getTitle))
                .setBlank(field(UpdateExpenseRequest::getParticipantsIds))
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Participants is empty"));
    }

    @Test
    @DisplayName("Should return 200 when update with same payer id")
    public void test15() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC));
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(payer.getId()));
        UpdateExpenseRequest body = Instancio.of(UpdateExpenseRequest.class)
                .setBlank(field(UpdateExpenseRequest::getParticipantsIds))
                .set(field(UpdateExpenseRequest::getPayerId), payer.getId())
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}", expense.getId())
                .then()
                .statusCode(200)
                .body("activityId", is(activity.getId().toString()))
                .body("amountInCents", is(body.getAmountInCents()))
                .body("id", is(expense.getId().toString()))
                .body("name", is(body.getTitle()))
                .body("createdAt", notNullValue())
                .body("participants.userId", hasItems(payer.getId().toString()))
                .body("participants.userName", hasItems(payer.getName()))
                .body("participants.amountOwedInCents", hasItems(body.getAmountInCents()))
                .body("payerName", is(payer.getName()));
    }

    @Test
    @DisplayName("Should return 400 when invalid body on update payer")
    public void test16() {
        Auth auth = token();
        UpdatePayerExpenseRequest body = new UpdatePayerExpenseRequest();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}/payer", UUID.randomUUID())
                .then()
                .statusCode(400)
                .body("detail", is("Um ou mais campos estão invalidos. Faça o preenchimento correto e tente novamente"))
                .body("objects.name", hasItems("payerId"));
    }

    @Test
    @DisplayName("Should return 404 when expense not found when update payer")
    public void test17() {
        Auth auth = token();
        UpdatePayerExpenseRequest body = Instancio.create(UpdatePayerExpenseRequest.class);
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}/payer", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Expense not found"));
    }

    @Test
    @DisplayName("Should return 400 when session user not is a participant on update payer")
    public void test18() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(userC);
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of( payer.getId()));
        UpdatePayerExpenseRequest body = Instancio.create(UpdatePayerExpenseRequest.class);
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}/payer", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("User is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 400 when payer not found on update payer")
    public void test20() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                null, activity, 1000L, List.of( auth.getUser().getId()));
        UpdatePayerExpenseRequest body = Instancio.create(UpdatePayerExpenseRequest.class);
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}/payer", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Payer not found"));
    }

    @Test
    @DisplayName("Should return 400 when payer not found on update payer")
    public void test21() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        User payer = createUserC();
        Expense expense = createExpenseForActivity(
                null, activity, 1000L, List.of( auth.getUser().getId()));
        UpdatePayerExpenseRequest body = Instancio.of(UpdatePayerExpenseRequest.class)
                .set(field(UpdatePayerExpenseRequest::getPayerId), payer.getId())
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}/payer", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Payer is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 200 on update payer")
    public void test22() {
        Auth auth = token();
        User payer = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(payer));
        Expense expense = createExpenseForActivity(
                null, activity, 1000L, List.of( auth.getUser().getId()));
        UpdatePayerExpenseRequest body = Instancio.of(UpdatePayerExpenseRequest.class)
                .set(field(UpdatePayerExpenseRequest::getPayerId), payer.getId())
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{id}/payer", expense.getId())
                .then()
                .statusCode(200)
                .body("id", is(expense.getId().toString()))
                .body("name", is(expense.getName()))
                .body("payerId", is(payer.getId().toString()))
                .body("payerName", is(payer.getName()))
                .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("Should return 400 when body is invalid  on register payment")
    public void test23() {
        Auth auth = token();
        MarkPaymentRequest body = new MarkPaymentRequest();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}/payments",UUID.randomUUID())
                .then()
                .statusCode(400)
                .body("detail", is("Um ou mais campos estão invalidos. Faça o preenchimento correto e tente novamente"))
                .body("objects.name", hasItems("amountInCents"));
    }

    @Test
    @DisplayName("Should return 404 when expense not found  on register payment")
    public void test24() {
        Auth auth = token();
        MarkPaymentRequest body = Instancio.create(MarkPaymentRequest.class);
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}/payments", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Expense not found"));
    }

    @Test
    @DisplayName("Should return 400 when user is not participant on register payment")
    public void test25() {
        Auth auth = token();
        User userB = createUserB();
        Activity activity = createActivity(userB);
        Expense expense = createExpenseForActivity(null, activity, 1000L, List.of(userB.getId()));
        MarkPaymentRequest body = Instancio.create(MarkPaymentRequest.class);
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}/payments", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Session user is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 400 when payment amount is greater than expense amount on register payment")
    public void test26() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(null, activity, 1000L, List.of(auth.getUser().getId()));
        MarkPaymentRequest body = Instancio.of(MarkPaymentRequest.class)
                .generate(field(MarkPaymentRequest::getAmountInCents), generators -> generators.longs().min(1000L))
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}/payments", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Payment amount exceeds the debt amount"));
    }

    @Test
    @DisplayName("Should return 201 register payment")
    public void test27() {
        Auth auth = token();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(null, activity, 1000L, List.of(auth.getUser().getId()));
        MarkPaymentRequest body = Instancio.of(MarkPaymentRequest.class)
                .generate(field(MarkPaymentRequest::getAmountInCents), generators -> generators.longs().max(999L))
                .create();
        given()
                .auth().oauth2(auth.getToken())
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post("/expenses/{id}/payments", expense.getId())
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("expenseId", is(expense.getId().toString()))
                .body("paidAt", notNullValue())
                .body("debtorName", is(auth.getUser().getName()))
                .body("amountPaidInCents", is(body.getAmountInCents().intValue()))
                .body("debtorId", is(auth.getUser().getId().toString()));
    }

    @Test
    @DisplayName("Should return 404 when expense not exits on toggle payment")
    public void test28() {
        Auth auth = token();
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", UUID.randomUUID(), UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("Expense not found"));

    }

    @Test
    @DisplayName("Should return 400 when session user not has a participant on toggle payment")
    public void test29() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(userC);
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(payer.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", expense.getId(), UUID.randomUUID())
                .then()
                .statusCode(400)
                .body("detail", is("Session user is not participant of this activity"));

    }

    @Test
    @DisplayName("Should return 400 when participant  not exits on toggle payment")
    public void test30() {
        Auth auth = token();
        User payer = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(payer.getId(),auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", expense.getId(), UUID.randomUUID())
                .then()
                .statusCode(400)
                .body("detail", is("Payer not found"));
    }

    @Test
    @DisplayName("Should return 400 when  participant is not a payment participant on toggle payment")
    public void test31() {
        Auth auth = token();
        User payer = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", expense.getId(), payer.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Payer is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 200 when success and has one participant on toggle payment")
    public void test32() {
        Auth auth = token();
        User payer = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", expense.getId(), auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("amountPaidInCents", is(1000))
                .body("remainingDebtInCents", is(0))
                .body("participantName", is(auth.getUser().getName()))
                .body("paymentStatus", is("pending"))
                .body("participantEmail", is(auth.getUser().getEmail()))
                .body("amountOwedInCents", is(1000))
                .body("expenseId", is(expense.getId().toString()))
                .body("participantId", is(auth.getUser().getId().toString()));
    }

    @Test
    @DisplayName("Should return 200 when success and has one participant on toggle payment")
    public void test33() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userC));
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(auth.getUser().getId(), userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", expense.getId(), userC.getId())
                .then()
                .statusCode(200)
                .body("amountPaidInCents", is(1000/2))
                .body("remainingDebtInCents", is(0))
                .body("participantName", is(userC.getName()))
                .body("paymentStatus", is("pending"))
                .body("participantEmail", is(userC.getEmail()))
                .body("amountOwedInCents", is(1000/2))
                .body("expenseId", is(expense.getId().toString()))
                .body("participantId", is(userC.getId().toString()));
    }

    @Test
    @DisplayName("Should return 200 when success and has one participant on toggle payment")
    public void test34() {
        Auth auth = token();
        User payer = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(auth.getUser().getId()));
        createExpensePayment(expense, 1000L, auth.getUser());
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .put("/expenses/{expenseId}/participants/{participantId}/payment/toggle", expense.getId(), auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("amountPaidInCents", is(0))
                .body("remainingDebtInCents", is(0))
                .body("participantName", is(auth.getUser().getName()))
                .body("paymentStatus", is("paid"))
                .body("participantEmail", is(auth.getUser().getEmail()))
                .body("amountOwedInCents", is(1000))
                .body("expenseId", is(expense.getId().toString()))
                .body("participantId", is(auth.getUser().getId().toString()));
    }

    @Test
    @DisplayName("Should return 404 when expense not exists on delete")
    public void test41() {
        Auth auth = token();

        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/expenses/{expenseId}", UUID.randomUUID().toString())
                .then()
                .statusCode(404)
                .body("detail", is("Expense not found"));
    }

    @Test
    @DisplayName("Should return 400 when session user is not a activity participant when delete expense")
    public void test42() {
        Auth auth = token();
        User payer = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(userC);
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/expenses/{expenseId}", expense.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Session user is not participant of this activity"));
    }

    @Test
    @DisplayName("Should return 204 on delete")
    public void test43() {
        Auth auth = token();
        User payer = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(
                payer.getId(), activity, 1000L, List.of(auth.getUser().getId()));
        createExpensePayment(expense, 1000L, auth.getUser());

        given()
                .auth().oauth2(auth.getToken())
                .contentType(ContentType.JSON)
                .when()
                .delete("/expenses/{expenseId}", expense.getId())
                .then()
                .statusCode(204);

        Optional<Expense> byId = expenseRepository.findById(expense.getId());
        assertTrue(byId.isEmpty());
        Set<ExpenseParticipant> byExpenseId = expenseParticipantRepository.findByExpenseId(expense.getId());
        assertTrue(byExpenseId.isEmpty());
        Set<ExpensePayment> expensePayments = expensePaymentRepository.findByExpenseId(expense.getId());
        assertTrue(expensePayments.isEmpty());
    }
}
