package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.requests.CreateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.MarkPaymentRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdatePayerExpenseRequest;
import dev.fsantana.expensesplitapi.api.responses.CreateExpenseResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseDetailResponse;
import dev.fsantana.expensesplitapi.api.responses.ToggleParticipantPaymentResponse;
import dev.fsantana.expensesplitapi.api.responses.UpdateExpensePaymentResponse;
import dev.fsantana.expensesplitapi.api.responses.UpdatePayerExpanseResponse;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.services.ExpenseService;
import dev.fsantana.expensesplitapi.domain.services.dto.ToggleParticipantPayment;
import dev.fsantana.expensesplitapi.utils.mappers.ExpenseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpensesController {

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @PostMapping("/{activityId}")
    public ResponseEntity<CreateExpenseResponse> createExpense(@PathVariable UUID activityId,@Valid @RequestBody CreateExpenseRequest request ) {
        Expense expense = expenseMapper.toModel(request, activityId);
        Expense save = expenseService.save(expense);
        CreateExpenseResponse createResponse = expenseMapper.toCreateResponse(save);
        return ResponseEntity.status(HttpStatus.CREATED).body(createResponse);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDetailResponse>  findByExpenseId(@PathVariable UUID expenseId){
        Expense expense = expenseService.loadById(expenseId);
        ExpenseDetailResponse result = expenseMapper.toExpenseDetailResponse(expense);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<CreateExpenseResponse> updateExpenseById(@PathVariable UUID expenseId,@Valid @RequestBody UpdateExpenseRequest request){
        Expense expense = expenseMapper.fromUpdateToModel(request, expenseId);
        expense.setId(expenseId);
        Expense update = expenseService.update(expense);
        CreateExpenseResponse createResponse = expenseMapper.toCreateResponse(update);
        return ResponseEntity.ok(createResponse);
    }

    @PutMapping("/{expenseId}/payer")
    public ResponseEntity<UpdatePayerExpanseResponse> updatePayer(@PathVariable UUID expenseId,@Valid @RequestBody UpdatePayerExpenseRequest request){
        Expense expense = expenseMapper.fromPayerUpdateToModel(request, expenseId);
        Expense update = expenseService.updatePayer(expense);
        UpdatePayerExpanseResponse updatePayerExpanseResponse = expenseMapper.fromModelToUpdatePayerExpanseResponse(update);
        return  ResponseEntity.ok(updatePayerExpanseResponse);
    }

    @PostMapping("/{expenseId}/payments")
    public ResponseEntity<UpdateExpensePaymentResponse> updatePayment(
            @PathVariable UUID expenseId,
            @Valid @RequestBody MarkPaymentRequest request ){
        ExpensePayment update = expenseService.updatePayment(expenseId, request.getAmountInCents());
        UpdateExpensePaymentResponse updatePayerExpanseResponse = expenseMapper.toExpensePaymentResponse(update);
        return  ResponseEntity.ok(updatePayerExpanseResponse);
    }

    @PutMapping("/{expenseId}/participants/{participantId}/payment/toggle")
    public ResponseEntity<ToggleParticipantPaymentResponse> toggleParticipantPayment(@PathVariable UUID expenseId,@PathVariable UUID participantId){
        ToggleParticipantPayment toggleParticipantPayment = expenseService.toggleExpenseParticipantPayment(expenseId, participantId);
        ToggleParticipantPaymentResponse response  = expenseMapper.toToggleParticipantPaymentResponse(toggleParticipantPayment);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId){
        expenseService.delete(expenseId);
        return ResponseEntity.noContent().build();
    }
}
