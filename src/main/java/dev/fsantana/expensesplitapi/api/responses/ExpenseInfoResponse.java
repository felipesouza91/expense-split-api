package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseInfoResponse {
    private UUID id;
    private String name;
    private Long amountInCents;
    private String payerName;
    private UUID payerId;
    private List<ExpenseInfoParticipantResponse> participants;
    private String paymentStatus;
}
