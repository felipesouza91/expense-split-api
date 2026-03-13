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
public class CreateExpenseResponse {

    private UUID id;
    private String name;
    private Integer amountInCents;
    private UUID payerId;
    private String payerName;
    private UUID activityId;
    private List<ParticipantDebt> participants;
    private OffsetDateTime createdAt;
}
