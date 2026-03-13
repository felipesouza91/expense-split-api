package dev.fsantana.expensesplitapi.domain.services.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DetailedBalance {
    private Long totalOwedToUserInCents;
    private Long totalUserOwesInCents;
    private List<DebtDetail> debts;
    private List<CreditDetail> credits;
}
