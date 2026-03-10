package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, UUID> {

    List<ExpenseParticipant> findByUserId(UUID debtorId);

    Set<ExpenseParticipant> findByExpenseId(UUID id);

    Optional<ExpenseParticipant> findByExpenseIdAndUserId(UUID expenseId, UUID userId);
}
