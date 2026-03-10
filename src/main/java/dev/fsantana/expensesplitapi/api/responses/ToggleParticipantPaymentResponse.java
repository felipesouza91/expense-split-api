package dev.fsantana.expensesplitapi.api.responses;

import lombok.Data;

import java.util.UUID;

@Data
public class ToggleParticipantPaymentResponse {
    private UUID expenseId;
    private UUID participantId;
    private String participantName;
    private String participantEmail;
    private Long amountOwedInCents;
    private Long amountPaidInCents;
    private Long remainingDebtInCents;
    private String paymentStatus;
}
