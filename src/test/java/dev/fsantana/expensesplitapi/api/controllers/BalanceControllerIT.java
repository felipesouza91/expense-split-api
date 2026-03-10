package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.configs.TestIntegrationConfig;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.security.models.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class BalanceControllerIT extends TestIntegrationConfig {


    @BeforeEach
    public void setup() {
        activityDataProvider.deleteAll();
    }

    @Test
    @DisplayName("Should return 400 when usertTo and userFrom is not session user on balance between")
    public void test0(){
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}",  userB.getId(),userC.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Invalid user"));
    }

    @Test
    @DisplayName("Should return 404 when user not found on balance between")
    public void test1(){
        Auth auth = token();
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", auth.getUser().getId(), UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("User not found"));
    }

    @Test
    @DisplayName("Should return 404 when user not found on balance between")
    public void test2(){
        Auth auth = token();
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", UUID.randomUUID(),auth.getUser().getId())
                .then()
                .statusCode(404)
                .body("detail", is("User not found"));
    }

    @Test
    @DisplayName("Should return 400 when usertTo and userFrom do not have transaction between on balance between")
    public void test3(){
        Auth auth = token();
        User userB = createUserB();
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", auth.getUser().getId(), userB.getId())
                .then()
                .statusCode(400)
                .body("detail", is("Users do not have transactions"));
    }


    @Test
    @DisplayName("Should return 200 with values on balance between")
    public void test4() {
        Auth auth = token();
        User userB = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", auth.getUser().getId(), userB.getId())
                .then()
                .statusCode(200)
                .body("netBalance.amountInCents", is(expense.getAmountInCents().intValue()))
                .body("netBalance.debtor.userId", is(userB.getId().toString()))
                .body("netBalance.debtor.name", is(userB.getName()))
                .body("netBalance.creditor.userId", is(auth.getUser().getId().toString()))
                .body("netBalance.creditor.name", is(auth.getUser().getName()))
                .body("details.amountInCents", hasItems(expense.getAmountInCents().intValue()))
                .body("details.activityId", hasItems(activity.getId().toString()))
                .body("details.toUser", hasItems(auth.getUser().getName()))
                .body("details.activityName", hasItems(activity.getName()))
                .body("details.fromUser", hasItems(userB.getName()))
        ;
    }

    @Test
    @DisplayName("Should return 200 with values on balance between")
    public void test5() {
        Auth auth = token();
        User userB = createUserB();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", userB.getId(),auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("netBalance.amountInCents", is(expense.getAmountInCents().intValue()))
                .body("netBalance.debtor.userId", is(userB.getId().toString()))
                .body("netBalance.debtor.name", is(userB.getName()))
                .body("netBalance.creditor.userId", is(auth.getUser().getId().toString()))
                .body("netBalance.creditor.name", is(auth.getUser().getName()))
                .body("details.amountInCents", hasItems(expense.getAmountInCents().intValue()))
                .body("details.activityId", hasItems(activity.getId().toString()))
                .body("details.toUser", hasItems(auth.getUser().getName()))
                .body("details.activityName", hasItems(activity.getName()))
                .body("details.fromUser", hasItems(userB.getName()))
        ;
    }

    @Test
    @DisplayName("Should return 200 with details length 0 on balance between")
    public void test6() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB, userC));
        List<UUID> participantsIds = List.of( userC.getId());
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, participantsIds);
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", userB.getId(),auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("details.size()", is(0));
    }

    @Test
    @DisplayName("Should return 200 with values on balance between")
    public void test7() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(userC.getId(), activity, 1000L, List.of(userB.getId(), userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", auth.getUser().getId(), userC.getId())
                .then()
                .statusCode(200)
                .body("details.size()", is(0));
    }

    @Test
    @DisplayName("Should return 200 with values on balance between")
    public void test8() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivity(auth.getUser());
        Expense expense = createExpenseForActivity(userC.getId(), activity, 1000L, List.of(userB.getId(), userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/between/{userIdFrom}/{userIdTo}", userC.getId(),auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("details.size()", is(0));
    }

    @Test
    @DisplayName("Should return 404 when user id not exits on balance global")
    public void test9() {
        Auth auth = token();

        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/global", UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("detail", is("User not found"));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance global")
    public void test10() {
        Auth auth = token();
        User userB = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/global", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("compensatedCredits.activities.activityId", hasItems(hasItems(activity.getId().toString())))
                .body("compensatedCredits.activities.activityName", hasItems(hasItems(activity.getName())))
                .body("compensatedCredits.activities.amountInCents", hasItems(hasItems(-expense.getAmountInCents().intValue())))
                .body("compensatedCredits.netAmountInCents", hasItems(expense.getAmountInCents().intValue()))
                .body("compensatedCredits.debtorId", hasItems(userB.getId().toString()))
                .body("compensatedCredits.activitiesCount", hasItems(1))
                .body("compensatedCredits.debtorName", hasItems(userB.getName()))
                .body("globalNetBalanceInCents", is(-expense.getAmountInCents().intValue()))
                .body("compensatedDebts.size()", is(0));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance global")
    public void test11() {
        Auth auth = token();
        User userB = createUserB();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/global", userB.getId())
                .then()
                .statusCode(400)
                .body("detail", is("User balance request is not the current user"));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance global")
    public void test12() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userC.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/global", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("compensatedCredits.activities.activityId", hasItems(hasItems(activity.getId().toString())))
                .body("compensatedCredits.activities.activityName", hasItems(hasItems(activity.getName())))
                .body("compensatedCredits.activities.amountInCents", hasItems(hasItems(-expense.getAmountInCents().intValue())))
                .body("compensatedCredits.netAmountInCents", hasItems(expense.getAmountInCents().intValue()))
                .body("compensatedCredits.debtorId", hasItems(userC.getId().toString()))
                .body("compensatedCredits.activitiesCount", hasItems(1))
                .body("compensatedCredits.debtorName", hasItems(userC.getName()))
                .body("globalNetBalanceInCents", is(-expense.getAmountInCents().intValue()))
                .body("compensatedDebts.size()", is(0));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance global")
    public void test13() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB,userC));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userC.getId(), userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/global", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("compensatedCredits.activities.activityId", hasItems(hasItems(activity.getId().toString())))
                .body("compensatedCredits.activities.activityName", hasItems(hasItems(activity.getName())))
                .body("compensatedCredits.activities.amountInCents", hasItems(hasItems(-expense.getAmountInCents().intValue()/2)))
                .body("compensatedCredits.netAmountInCents", hasItems(expense.getAmountInCents().intValue()/2))
                .body("compensatedCredits.debtorId", hasItems(userC.getId().toString(),userB.getId().toString() ))
                .body("compensatedCredits.activitiesCount", hasItems(1))
                .body("compensatedCredits.debtorName", hasItems(userC.getName(),userB.getName()))
                .body("globalNetBalanceInCents", is(-expense.getAmountInCents().intValue()))
                .body("compensatedDebts.size()", is(0));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance global")
    public void test14() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userB.getId()));
        Activity activity2 = createActivityWithParticipants(userB, Set.of(auth.getUser()));
        Expense expense2 = createExpenseForActivity(userB.getId(), activity2, 400L, List.of(auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/global", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("compensatedCredits.activities.activityId", hasItems(hasItems(activity.getId().toString(), activity2.getId().toString())))
                .body("compensatedCredits.activities.activityName", hasItems(hasItems(activity.getName(), activity2.getName())))
                .body("compensatedCredits.activities.amountInCents", hasItems(hasItems(-expense.getAmountInCents().intValue(), expense2.getAmountInCents().intValue())))
                .body("compensatedCredits.netAmountInCents", hasItems(expense.getAmountInCents().intValue()-expense2.getAmountInCents().intValue()))
                .body("compensatedCredits.debtorId", hasItems(userB.getId().toString() ))
                .body("compensatedCredits.activitiesCount", hasItems(2))
                .body("compensatedCredits.debtorName", hasItems( userB.getName() ))
                .body("globalNetBalanceInCents", is(-expense.getAmountInCents().intValue() +  expense2.getAmountInCents().intValue()))
                .body("compensatedDebts.size()", is(0));
    }

    @Test
    @DisplayName("Should return 400 when userid is different of session id on balance detailed")
    public void test15() {
        Auth auth = token();
        User userB = createUserB();

        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/detailed", userB.getId())
                .then()
                .statusCode(400)
                .body("detail", is("User balance request is not the current user"));
    }
    @Test
    @DisplayName("Should return 200 when user id exits on balance detailed")
    public void test16() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB,userC));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userC.getId(), userB.getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/detailed", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("totalUserOwesInCents", is(0))
                .body("credits.amountInCents", hasItems(expense.getAmountInCents().intValue()/2))
                .body("credits.debtorName", hasItems(userB.getName(),userC.getName()))
                .body("credits.expenseName", hasItems(expense.getName()))
                .body("credits.expenseId", hasItems(expense.getId().toString()))
                .body("credits.activityId", hasItems(activity.getId().toString()))
                .body("credits.activityName", hasItems(activity.getName()))
                .body("credits.debtorId", hasItems(userB.getId().toString(), userC.getId().toString()))
                .body("debts.size()", is(0))
                .body("totalOwedToUserInCents", is(expense.getAmountInCents().intValue()));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance detailed")
    public void test17() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB,userC));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userC.getId(), userB.getId()));
        Activity activity2 = createActivityWithParticipants(userB, Set.of(auth.getUser()));
        Expense expense2 = createExpenseForActivity(userB.getId(), activity2, 1000L, List.of(auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/detailed", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("totalUserOwesInCents", is(expense2.getAmountInCents().intValue()))
                .body("credits.amountInCents", hasItems(expense.getAmountInCents().intValue()/2))
                .body("credits.debtorName", hasItems(userB.getName(),userC.getName()))
                .body("credits.expenseName", hasItems(expense.getName()))
                .body("credits.expenseId", hasItems(expense.getId().toString()))
                .body("credits.activityId", hasItems(activity.getId().toString()))
                .body("credits.activityName", hasItems(activity.getName()))
                .body("credits.debtorId", hasItems(userB.getId().toString(), userC.getId().toString()))
                .body("debts.amountInCents", hasItems(expense2.getAmountInCents().intValue()))
                .body("debts.creditorName", hasItems(userB.getName()))
                .body("debts.creditorId", hasItems(userB.getId().toString()))
                .body("debts.expenseName", hasItems(expense2.getName()))
                .body("debts.expenseId", hasItems(expense2.getId().toString()))
                .body("debts.activityId", hasItems(activity2.getId().toString()))
                .body("debts.activityName", hasItems(activity2.getName()))
                .body("totalOwedToUserInCents", is(expense.getAmountInCents().intValue()));
    }

    @Test
    @DisplayName("Should return 200 when user id exits on balance detailed")
    public void test18() {
        Auth auth = token();
        User userB = createUserB();
        User userC = createUserC();
        Activity activity = createActivityWithParticipants(auth.getUser(), Set.of(userB,userC));
        Expense expense = createExpenseForActivity(auth.getUser().getId(), activity, 1000L, List.of(userC.getId(), userB.getId()));
        Activity activity2 = createActivityWithParticipants(userB, Set.of(auth.getUser()));
        Expense expense2 = createExpenseForActivity(null, activity2, 1000L, List.of(auth.getUser().getId()));
        given()
                .auth().oauth2(auth.getToken())
                .get("/balance/users/{userId}/detailed", auth.getUser().getId())
                .then()
                .statusCode(200)
                .body("totalUserOwesInCents", is(0))
                .body("credits.amountInCents", hasItems(expense.getAmountInCents().intValue()/2))
                .body("credits.debtorName", hasItems(userB.getName(),userC.getName()))
                .body("credits.expenseName", hasItems(expense.getName()))
                .body("credits.expenseId", hasItems(expense.getId().toString()))
                .body("credits.activityId", hasItems(activity.getId().toString()))
                .body("credits.activityName", hasItems(activity.getName()))
                .body("credits.debtorId", hasItems(userB.getId().toString(), userC.getId().toString()))
                .body("debts.size()", is(0))
                .body("totalOwedToUserInCents", is(expense.getAmountInCents().intValue()));
    }
}
