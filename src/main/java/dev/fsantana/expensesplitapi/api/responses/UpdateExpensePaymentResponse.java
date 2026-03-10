package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpensePaymentResponse {
    private UUID id;
    private UUID expenseId;
    private UUID debtorId;
    private String debtorName;
    private Long amountPaidInCents;
    private OffsetDateTime paidAt;
}
