package dev.fsantana.expensesplitapi.domain.services.dto;

import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreditDetail {
    private User debtor;
    private Activity activity;
    private Expense expense;
    private Long amountInCents;
}
