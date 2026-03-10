package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantInfoResponse {

    private UUID userId;
    private String name;
    private String email;
    private Long amountOwedInCents;
    private Long amountPaidInCents;
    private Long remainingDebtInCents;
    private String paymentStatus;

}
