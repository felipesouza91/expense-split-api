package dev.fsantana.expensesplitapi.api.responses.balances;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DetailedBalanceResponse {
    private Long totalOwedToUserInCents;
    private Long totalUserOwesInCents;
    private List<DebtDetailResponse> debts;
    private List<CreditDetailResponse> credits;
}
