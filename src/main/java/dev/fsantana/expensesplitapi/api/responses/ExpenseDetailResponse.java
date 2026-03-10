package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDetailResponse {

    private UUID id;
    private String name;
    private Long amountInCents;
    private PayerInfoResponse payer;
    private UUID activityId;
    private String activityName;
    private List<ParticipantInfoResponse> participants;
    private List<PaymentInfoResponse> payments;
    private OffsetDateTime createdAt;

}
