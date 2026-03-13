package dev.fsantana.expensesplitapi.utils.mappers;

import dev.fsantana.expensesplitapi.api.responses.BalanceBetweenUserResponse;
import dev.fsantana.expensesplitapi.api.responses.CompensatedCreditResponse;
import dev.fsantana.expensesplitapi.api.responses.CompensatedDebtResponse;
import dev.fsantana.expensesplitapi.api.responses.NetBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.UserGlobalBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.balances.CreditDetailResponse;
import dev.fsantana.expensesplitapi.api.responses.balances.DebtDetailResponse;
import dev.fsantana.expensesplitapi.api.responses.balances.DetailedBalanceResponse;
import dev.fsantana.expensesplitapi.domain.services.dto.BalanceBetweenUser;
import dev.fsantana.expensesplitapi.domain.services.dto.CompensatedCredit;
import dev.fsantana.expensesplitapi.domain.services.dto.CompensatedDebt;
import dev.fsantana.expensesplitapi.domain.services.dto.CreditDetail;
import dev.fsantana.expensesplitapi.domain.services.dto.DebtDetail;
import dev.fsantana.expensesplitapi.domain.services.dto.DetailedBalance;
import dev.fsantana.expensesplitapi.domain.services.dto.NetBalance;
import dev.fsantana.expensesplitapi.domain.services.dto.UserGlobalBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UserMapper.class})
public interface BalanceMapper {


    BalanceBetweenUserResponse toBalanceResponse(BalanceBetweenUser balanceBetweenUser);

    NetBalanceResponse toDto(NetBalance netBalance);

    UserGlobalBalanceResponse toUserGlobalBalanceResponse(UserGlobalBalance balance);

    @Mapping(source = "creditor.id", target = "creditorId")
    @Mapping(source = "creditor.name", target = "creditorName")
    CompensatedDebtResponse toCompensatedDebtResponse(CompensatedDebt compensatedDebt);

    @Mapping(source = "debtor.id", target = "debtorId")
    @Mapping(source = "debtor.name", target = "debtorName")
    CompensatedCreditResponse toCompensatedCreditResponse(CompensatedCredit compensatedDebt);

    DetailedBalanceResponse toDetailedBalanceResponse(DetailedBalance detailedBalanceByUser);

    @Mapping(source = "debtor.id", target = "debtorId")
    @Mapping(source = "debtor.name", target = "debtorName")
    @Mapping(source = "activity.id", target = "activityId")
    @Mapping(source = "activity.name", target = "activityName")
    @Mapping(source = "expense.id", target = "expenseId")
    @Mapping(source = "expense.name", target = "expenseName")
    CreditDetailResponse toCreditDetailResponse(CreditDetail creditDetail);

    @Mapping(source = "creditor.id", target = "creditorId")
    @Mapping(source = "creditor.name", target = "creditorName")
    @Mapping(source = "activity.id", target = "activityId")
    @Mapping(source = "activity.name", target = "activityName")
    @Mapping(source = "expense.id", target = "expenseId")
    @Mapping(source = "expense.name", target = "expenseName")
    DebtDetailResponse toDebtDetailResponse(DebtDetail debtDetail);
}
