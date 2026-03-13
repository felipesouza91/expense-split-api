package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NetBalanceResponse {
    private UserInfoResume debtor;
    private UserInfoResume creditor;
    private Long amountInCents;
}
