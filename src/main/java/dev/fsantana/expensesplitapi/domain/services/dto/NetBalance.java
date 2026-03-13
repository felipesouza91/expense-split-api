package dev.fsantana.expensesplitapi.domain.services.dto;

import dev.fsantana.expensesplitapi.domain.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetBalance {
    private User debtor;
    private User creditor;
    private Long amountInCents;
}
