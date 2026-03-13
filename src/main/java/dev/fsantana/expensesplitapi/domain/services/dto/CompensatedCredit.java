package dev.fsantana.expensesplitapi.domain.services.dto;

import dev.fsantana.expensesplitapi.domain.models.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CompensatedCredit {
    private User debtor;
    private Long netAmountInCents;
    private Long activitiesCount;
    private List<ActivityBreakdown> activities;
}
