package dev.fsantana.expensesplitapi.domain.services.dto;

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
public class UserGlobalBalance {
    private Long globalNetBalanceInCents;
    private Set<CompensatedDebt> compensatedDebts;
    private Set<CompensatedCredit> compensatedCredits;
}
