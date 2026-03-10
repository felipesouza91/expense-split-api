package dev.fsantana.expensesplitapi.domain.services.dto;

import dev.fsantana.expensesplitapi.domain.models.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class BalanceEachUser {
    private User user;
    private Long netAmount;
    private List<ActivityBreakdown> activities;
}
