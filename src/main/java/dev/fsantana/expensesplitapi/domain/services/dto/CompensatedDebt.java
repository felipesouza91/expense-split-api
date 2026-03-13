package dev.fsantana.expensesplitapi.domain.services.dto;

import dev.fsantana.expensesplitapi.domain.models.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CompensatedDebt {
    private User creditor;
    private Long netAmountInCents;
    private Long activitiesCount;
    private List<ActivityBreakdown> activities;
}
