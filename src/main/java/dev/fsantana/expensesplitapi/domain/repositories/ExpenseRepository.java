package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByPayerId(UUID id);
}
