package dev.fsantana.expensesplitapi.api.controllers.docs;

import dev.fsantana.expensesplitapi.api.requests.CreateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.MarkPaymentRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdatePayerExpenseRequest;
import dev.fsantana.expensesplitapi.api.responses.CreateExpenseResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseDetailResponse;
import dev.fsantana.expensesplitapi.api.responses.ToggleParticipantPaymentResponse;
import dev.fsantana.expensesplitapi.api.responses.UpdateExpensePaymentResponse;
import dev.fsantana.expensesplitapi.api.responses.UpdatePayerExpanseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Expense")
@SecurityRequirement(name = "security_auth")
@ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "500", description = "Server error",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
public interface ExpenseControllerOpenApi {

    @Operation(summary = "Create expense")
    @ApiResponse(responseCode = "201", description = "Expense data")
    @ApiResponse(responseCode = "400", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<CreateExpenseResponse> createExpense(
            @PathVariable UUID activityId,
            @Valid @RequestBody CreateExpenseRequest request );

    @Operation(summary = "Get expense")
    @ApiResponse(responseCode = "200", description = "Expense data")
    @ApiResponse(responseCode = "400", description = "User is not participant of this expense",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Expense not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ExpenseDetailResponse>  findByExpenseId(@PathVariable UUID expenseId);

    @Operation(summary = "Update expense")
    @ApiResponse(responseCode = "200", description = "Expense data")
    @ApiResponse(responseCode = "400", description = "User is not participant of this expense",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Expense not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<CreateExpenseResponse> updateExpenseById(
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdateExpenseRequest request);

    @Operation(summary = "Update expense payer")
    @ApiResponse(responseCode = "200", description = "Expense data")
    @ApiResponse(responseCode = "400", description = "Payer is not participant of this activity",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Expense not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<UpdatePayerExpanseResponse> updatePayer(
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdatePayerExpenseRequest request);

    @Operation(summary = "Update payment ")
    @ApiResponse(responseCode = "200", description = "Payment data")
    @ApiResponse(responseCode = "400", description = "Payment amount exceeds the debt amount",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Expense not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<UpdateExpensePaymentResponse> updatePayment(
            @PathVariable UUID expenseId,
            @Valid @RequestBody MarkPaymentRequest request );

    @Operation(summary = "Toggle payment participant")
    @ApiResponse(responseCode = "200", description = "Toggle Payment data")
    @ApiResponse(responseCode = "400", description = "Session user is not participant of this activity",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Expense not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ToggleParticipantPaymentResponse> toggleParticipantPayment(
            @PathVariable UUID expenseId,
            @PathVariable UUID participantId);

    @Operation(summary = "Delete expense")
    @ApiResponse(responseCode = "204", description = "Delete success")
    @ApiResponse(responseCode = "400", description = "Session user is not participant of this activity",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Expense not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId);
}
