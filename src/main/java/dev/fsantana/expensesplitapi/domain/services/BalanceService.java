package dev.fsantana.expensesplitapi.domain.services;

import dev.fsantana.expensesplitapi.domain.services.dto.BalanceBetweenUser;
import dev.fsantana.expensesplitapi.domain.services.dto.DetailedBalance;
import dev.fsantana.expensesplitapi.domain.services.dto.UserGlobalBalance;

import java.util.UUID;

public interface BalanceService {

    BalanceBetweenUser balanceBetweenUser(UUID userFrom, UUID userto);

    UserGlobalBalance loadGlobalBalanceByUserId(UUID userId);

    DetailedBalance getDetailedBalanceByUser(UUID userId);
}
