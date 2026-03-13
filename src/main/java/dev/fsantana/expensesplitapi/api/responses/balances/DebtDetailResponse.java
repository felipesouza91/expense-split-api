package dev.fsantana.expensesplitapi.api.responses.balances;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class DebtDetailResponse {
    private UUID creditorId;
    private String creditorName;
    private Long amountInCents;
    private UUID activityId;
    private String activityName;
    private UUID expenseId;
    private String expenseName;
}
