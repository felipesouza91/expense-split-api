package dev.fsantana.expensesplitapi.infra.repository;

import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.models.Statistics;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpensePaymentRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseRepository;
import dev.fsantana.expensesplitapi.domain.repositories.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepository {

    private final ExpensePaymentRepository expensePaymentRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final ExpenseRepository expenseRepository;
    private final ActivityParticipantRepository activityParticipantRepository;

    @Override
    public Statistics loadStatisticsById(UUID id) {
        List<ExpensePayment> payments = expensePaymentRepository.findByDebtorId(id);
        Long amountPaidInCents = payments.stream()
                .reduce(0L, (total, item) -> total + item.getAmountPaidInCents(), Long::sum);

        Set<UUID> paidExpenseIds = payments.stream().map(ExpensePayment::getId).collect(Collectors.toSet());
        Long paidExpensesCount  = (long) paidExpenseIds.size();

        List<ExpenseParticipant> expenseParticipants  = expenseParticipantRepository.findByUserId(id);

        Long amountToPayInCents = 0L;
        Set<UUID> expensesWithDebt = new HashSet<UUID>();

        for (ExpenseParticipant expenseParticipant : expenseParticipants) {
            List<ExpensePayment> expensePayments = expensePaymentRepository
                    .findByDebtorIdAndExpenseId( id, expenseParticipant.getExpense().getId());

            Long totalPaid = expensePayments.stream().reduce(0L, (total, item) -> total + item.getAmountPaidInCents(), Long::sum);
            Long remaining = expenseParticipant.getAmountOwedInCents() - totalPaid;

            if (remaining > 0) {
                amountToPayInCents += remaining;
                expensesWithDebt.add(expenseParticipant.getId());
            }
        }

        Long expensesToPayCount = (long) expensesWithDebt.size();

        Long totalExpensesAmountInCents = expenseParticipants.stream()
                .reduce(0L, (total, item)
                        -> item.getExpense().getAmountInCents() + total, Long::sum);


        Long activitiesCount = (long) activityParticipantRepository.findByUserId(id).size();

        Long expensesCount = (long) expenseParticipants.size();

        Set<UUID> uniqueParticipantsIds = new HashSet<>();
        for(ExpenseParticipant expenseParticipant : expenseParticipants) {
            Set<ExpenseParticipant> otherParticipants = expenseParticipantRepository.findByExpenseId(expenseParticipant.getExpense().getId());
            otherParticipants.stream()
                    .filter(item -> !item.getUser().getId().equals(id))
                    .forEach(item -> {
                        uniqueParticipantsIds.add(item.getUser().getId());
                    });
        }

        Long uniqueParticipantsCount = (long) uniqueParticipantsIds.size();
        return  Statistics.builder()
                .expensesToPayCount(expensesToPayCount)
                .amountToPayInCents(amountToPayInCents)
                .expensesCount(expensesCount)
                .amountPaidInCents(amountPaidInCents)
                .uniqueParticipantsCount(uniqueParticipantsCount)
                .totalExpensesAmountInCents(totalExpensesAmountInCents)
                .activitiesCount(activitiesCount)
                .paidExpensesCount(paidExpensesCount)
                .build();
    }
}
