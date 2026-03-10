package dev.fsantana.expensesplitapi.domain.services.dto;

import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToggleParticipantPayment {

    private Expense expense;
    private ExpenseParticipant participant;
    private Long amountOwedInCents;
    private Long amountPaidInCents;
    private Long remainingDebtInCents;
    private String paymentStatus;
}
