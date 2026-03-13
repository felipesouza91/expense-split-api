package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, UUID> {

    List<ExpensePayment> findByDebtorId(UUID debtorId);
    List<ExpensePayment> findByDebtorIdAndExpenseId(UUID debtorId, UUID expenseId);

    Set<ExpensePayment> findByExpenseId(UUID expenseId);
}
