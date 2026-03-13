package dev.fsantana.expensesplitapi.domain.services;

import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.services.dto.ToggleParticipantPayment;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {
    Expense save(Expense expense);

    List<Expense> listByActivityId(UUID activityId);

    Expense loadById(UUID expenseId);

    Expense update(Expense expense);

    Expense updatePayer(Expense expense);

    ExpensePayment updatePayment(UUID expenseId, Long amountInCents);

    ToggleParticipantPayment toggleExpenseParticipantPayment(UUID expenseId, UUID participantId);

    void delete(UUID expenseId);
}
