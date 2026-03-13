package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGlobalBalanceResponse {
    private Long globalNetBalanceInCents;
    private Set<CompensatedDebtResponse> compensatedDebts;
    private Set<CompensatedCreditResponse> compensatedCredits;
}
