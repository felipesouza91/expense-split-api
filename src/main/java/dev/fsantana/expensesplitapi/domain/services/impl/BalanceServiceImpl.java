package dev.fsantana.expensesplitapi.domain.services.impl;

import dev.fsantana.expensesplitapi.domain.exceptions.AppEntityNotFound;
import dev.fsantana.expensesplitapi.domain.exceptions.AppRuleException;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseRepository;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.domain.services.BalanceService;
import dev.fsantana.expensesplitapi.domain.services.dto.ActivityBreakdown;
import dev.fsantana.expensesplitapi.domain.services.dto.ActivityDetail;
import dev.fsantana.expensesplitapi.domain.services.dto.BalanceBetweenUser;
import dev.fsantana.expensesplitapi.domain.services.dto.BalanceEachUser;
import dev.fsantana.expensesplitapi.domain.services.dto.CompensatedCredit;
import dev.fsantana.expensesplitapi.domain.services.dto.CompensatedDebt;
import dev.fsantana.expensesplitapi.domain.services.dto.CreditDetail;
import dev.fsantana.expensesplitapi.domain.services.dto.DebtDetail;
import dev.fsantana.expensesplitapi.domain.services.dto.DetailedBalance;
import dev.fsantana.expensesplitapi.domain.services.dto.NetBalance;
import dev.fsantana.expensesplitapi.domain.services.dto.UserGlobalBalance;
import dev.fsantana.expensesplitapi.security.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final UserSessionService userSessionService;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final UserRepository   userRepository;
    private final ActivityRepository activityRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;

    @Override
    public BalanceBetweenUser balanceBetweenUser(UUID userFrom, UUID userTo) {
        User currentUser = userSessionService.getCurrentUser();
        if(!userFrom.equals(currentUser.getId()) && !userTo.equals(currentUser.getId())) {
            throw new AppRuleException("Invalid user");
        }

        User userFromData = userRepository.findById(userFrom).orElseThrow(() -> new AppEntityNotFound("User not found"));
        User userToData = userRepository.findById(userTo).orElseThrow(() -> new AppEntityNotFound("User not found"));

        Set<ActivityParticipant> activityParticipantsFrom = activityParticipantRepository.findByUserId(userFrom);
        Set<ActivityParticipant> activityParticipantsTo = activityParticipantRepository.findByUserId(userTo);
        Set<UUID> activityFrom = activityParticipantsFrom.stream()
                .map(item -> item.getActivity().getId())
                .collect(Collectors.toSet());
        Set<UUID> activityTo = activityParticipantsTo.stream()
                .map(item -> item.getActivity().getId())
                .collect(Collectors.toSet());
        Set<UUID> commonActivityIds = activityFrom.stream().filter(activityTo::contains).collect(Collectors.toSet());
        if( commonActivityIds.isEmpty() ) {
            throw new AppRuleException("Users do not have transactions");
        }

        List<ActivityDetail> details = new ArrayList<>();
        Long netBalance = 0L;

        for (UUID activityId : commonActivityIds) {
            Activity activity =  activityRepository.findById(activityId).orElseThrow(() -> new AppEntityNotFound("Activity not found"));
            Long activityBalance = 0L;
            for (Expense expense: activity.getExpenses()) {
                UUID playerId = expense.getPayer().getId();

                if (playerId.equals(userFrom)) {
                    Optional<ExpenseParticipant> result = expense.getExpenseParticipants()
                            .stream().filter(item -> item.getUser().getId().equals(userTo)).findFirst();
                    if (result.isPresent()) {
                        activityBalance -= result.get().getAmountOwedInCents();
                    }
                }

                if (playerId.equals(userTo)) {
                    Optional<ExpenseParticipant> result = expense.getExpenseParticipants()
                            .stream().filter(item -> item.getUser().getId().equals(userFrom)).findFirst();
                    if (result.isPresent()) {
                        activityBalance += result.get().getAmountOwedInCents();
                    }
                }

            }

            if (activityBalance != 0) {
                String fromUser = activityBalance > 0 ? userFromData.getName() : userToData.getName();
                String toUser = activityBalance > 0 ? userToData.getName() : userFromData.getName();

                details.add(ActivityDetail.builder()
                        .activityId(activityId)
                        .activityName(activity.getName())
                        .fromUser(fromUser)
                        .toUser(toUser)
                        .amountInCents(Math.abs(activityBalance))
                        .build()
                );

                netBalance += activityBalance;
            }
        }

        NetBalance netBalanceData = new NetBalance();

        if (netBalance != 0) {
            User debtor = netBalance > 0 ? userFromData : userToData;
            User creditor = netBalance > 0 ? userToData:  userFromData ;

            netBalanceData.setCreditor(creditor);
            netBalanceData.setDebtor(debtor);
            netBalanceData.setAmountInCents(Math.abs(netBalance));
        }

        return BalanceBetweenUser.builder().netBalance(netBalanceData).details(details).build();
    }

    @Override
    public UserGlobalBalance loadGlobalBalanceByUserId(UUID userId) {
        User currentUser = userSessionService.getCurrentUser();
        User user =  userRepository.findById(userId).orElseThrow(() -> new AppEntityNotFound("User not found"));
        if(!currentUser.getId().equals(user.getId())) {
            throw new AppRuleException("User balance request is not the current user");
        }
       return calculateUserGlobalBalance(userId);
    }

    @Override
    public DetailedBalance getDetailedBalanceByUser(UUID userId) {
        User currentUser = userSessionService.getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new AppRuleException("User balance request is not the current user");
        }
        List<Expense> expensesAsPayer =  expenseRepository.findByPayerId(currentUser.getId());
        List<ExpenseParticipant> expenseParticipants = expenseParticipantRepository.findByUserId(userId);

        List<CreditDetail> credits =  new ArrayList<>();
        List<DebtDetail> debts = new ArrayList<>();

        for (Expense expense: expensesAsPayer) {
            expense.getExpenseParticipants()
                    .stream().filter(item -> item.getExpense().getId().equals(expense.getId()))
                    .forEach(debtor -> {
                        credits.add(CreditDetail.builder()
                                .debtor(debtor.getUser())
                                .activity(debtor.getExpense().getActivity())
                                .expense(debtor.getExpense())
                                .amountInCents(debtor.getAmountOwedInCents())
                                .build()
                        );
                    });
        }
        for (ExpenseParticipant ep: expenseParticipants) {
            if(ep.getExpense().getPayer() != null) {
                debts.add(DebtDetail.builder()
                        .creditor(ep.getExpense().getPayer())
                        .activity(ep.getExpense().getActivity())
                        .expense(ep.getExpense())
                        .amountInCents(ep.getAmountOwedInCents())
                        .build()
                );
            }
        }

        Long totalCredit = credits.stream().reduce(0L, (total, value) -> value.getAmountInCents() + total, Long::sum);
        Long totalDebt = debts.stream().reduce(0L, (total, value) -> value.getAmountInCents() + total, Long::sum);

        return DetailedBalance.builder()
                .totalOwedToUserInCents(totalCredit)
                .totalUserOwesInCents(totalDebt)
                .debts(debts)
                .credits(credits)
                .build();
    }

    private UserGlobalBalance calculateUserGlobalBalance(UUID userId) {
        UserGlobalBalance balance = new UserGlobalBalance();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppEntityNotFound("User not found"));
        Set<ActivityParticipant> activityParticipants = activityParticipantRepository.findByUserId(userId);
        Map<UUID, BalanceEachUser> allUsersIds = new HashMap<>();
        activityParticipants.forEach(activityParticipant -> {
            for (ActivityParticipant participant : activityParticipant.getActivity().getParticipants()) {
                if(!participant.getUser().getId().equals(userId)) {
                    allUsersIds.put(participant.getUser().getId(), null);
                }
            }
        });
        for ( Map.Entry<UUID, BalanceEachUser> entry : allUsersIds.entrySet()) {
            BalanceBetweenUser balanceBetweenUser = balanceBetweenUser(userId, entry.getKey());
            Optional<User> otherUser = userRepository.findById(entry.getKey());
            if (!balanceBetweenUser.getDetails().isEmpty()  && otherUser.isPresent()) {
                List<ActivityBreakdown> activities = balanceBetweenUser.getDetails().stream()
                        .map(item ->  {
                            Long amountInCents = item.getAmountInCents();
                            if ( !item.getFromUser().equals(user.getName())) {
                                amountInCents = -amountInCents;
                            }
                            return ActivityBreakdown.builder()
                                    .activityId(item.getActivityId())
                                    .activityName(item.getActivityName())
                                    .amountInCents(amountInCents)
                                    .build();
                            }
                        ).toList();
                Long netAmount = 0L;
                if (balanceBetweenUser.getNetBalance().getDebtor().getId().equals(userId)  ) {
                    netAmount = balanceBetweenUser.getNetBalance().getAmountInCents();
                } else {
                    netAmount = -balanceBetweenUser.getNetBalance().getAmountInCents();
                }
                entry.setValue(BalanceEachUser.builder().user(otherUser.get()).netAmount(netAmount).activities(activities).build());

            }
            Set<CompensatedDebt> compensatedDebts = new HashSet<>();
            Set<CompensatedCredit> compensatedCredits = new HashSet<>();
            Long globalNetBalance = 0L;

            for ( Map.Entry<UUID, BalanceEachUser> data : allUsersIds.entrySet()) {
                if (data.getValue() != null) {
                    globalNetBalance += data.getValue().getNetAmount();

                    if (data.getValue().getNetAmount() > 0) {
                        compensatedDebts.add(CompensatedDebt.builder()
                                .creditor(data.getValue().getUser())
                                .netAmountInCents(data.getValue().getNetAmount())
                                .activitiesCount((long)data.getValue().getActivities().size())
                                .activities(data.getValue().getActivities())
                                .build());
                    } else if (data.getValue().getNetAmount() < 0) {
                        compensatedCredits.add(CompensatedCredit.builder()
                                .debtor(data.getValue().getUser())
                                .netAmountInCents(Math.abs(data.getValue().getNetAmount()))
                                .activitiesCount((long)data.getValue().getActivities().size())
                                .activities(data.getValue().getActivities())
                                .build());
                    }
                }
            }
            balance = UserGlobalBalance.builder()
                    .globalNetBalanceInCents(globalNetBalance)
                    .compensatedDebts(compensatedDebts)
                    .compensatedCredits(compensatedCredits)
                    .build();

        }
        return balance;
    }
}
