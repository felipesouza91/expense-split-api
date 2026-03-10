package dev.fsantana.expensesplitapi.domain.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExpenseParticipantTest {

    private UUID participantId;
    private UUID expenseId;
    private UUID userId;

    private User user;
    private Expense expense;
    private ExpenseParticipant participant;

    @BeforeEach
    void setUp() {
        participantId = UUID.randomUUID();
        expenseId     = UUID.randomUUID();
        userId        = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash");

        expense = new Expense();
        expense.setId(expenseId);
        expense.setName("Dinner");
        expense.setAmountInCents(5000L);

        participant = new ExpenseParticipant();
        participant.setId(participantId);
        participant.setAmountOwedInCents(2500L);
        participant.setUser(user);
        participant.setExpense(expense);
    }

    // ── null / type guard ────────────────────────────────────────────────────

    @Test
    void equals_nullObject_returnsFalse() {
        assertFalse(participant.equals(null));
    }

    @Test
    void equals_differentClass_returnsFalse() {
        assertFalse(participant.equals("not an ExpenseParticipant"));
    }

    // ── null fields on the *other* object ────────────────────────────────────

    @Test
    void equals_otherHasNullUser_returnsFalse() {
        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(participantId);
        other.setExpense(expense);
        other.setUser(null);

        assertFalse(participant.equals(other));
    }

    @Test
    void equals_otherHasNullExpense_returnsFalse() {
        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(participantId);
        other.setUser(user);
        other.setExpense(null);

        assertFalse(participant.equals(other));
    }

    @Test
    void equals_otherHasNullUserAndNullExpense_returnsFalse() {
        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(participantId);
        other.setUser(null);
        other.setExpense(null);

        assertFalse(participant.equals(other));
    }

    // ── same reference ───────────────────────────────────────────────────────

    @Test
    void equals_sameReference_returnsTrue() {
        assertTrue(participant.equals(participant));
    }

    // ── all fields match ─────────────────────────────────────────────────────

    @Test
    void equals_sameIdUserIdAndExpenseId_returnsTrue() {
        // Distinct object instances that share the same IDs
        User sameUser = new User();
        sameUser.setId(userId);
        sameUser.setName("Alice");
        sameUser.setEmail("alice@example.com");
        sameUser.setPasswordHash("hash");

        Expense sameExpense = new Expense();
        sameExpense.setId(expenseId);
        sameExpense.setName("Dinner");
        sameExpense.setAmountInCents(5000L);

        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(participantId);
        other.setAmountOwedInCents(9999L); // irrelevant field — should not affect equality
        other.setUser(sameUser);
        other.setExpense(sameExpense);

        assertTrue(participant.equals(other));
    }

    // ── id mismatch ──────────────────────────────────────────────────────────

    @Test
    void equals_differentParticipantId_returnsFalse() {
        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(UUID.randomUUID()); // different
        other.setUser(user);
        other.setExpense(expense);

        assertFalse(participant.equals(other));
    }

    @Test
    void equals_nullIdOnBothSides_returnsTrue() {
        participant.setId(null);

        User sameUser = new User();
        sameUser.setId(userId);
        sameUser.setName("Alice");
        sameUser.setEmail("alice@example.com");
        sameUser.setPasswordHash("hash");

        Expense sameExpense = new Expense();
        sameExpense.setId(expenseId);
        sameExpense.setName("Dinner");
        sameExpense.setAmountInCents(5000L);

        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(null);
        other.setUser(sameUser);
        other.setExpense(sameExpense);

        assertTrue(participant.equals(other));
    }

    @Test
    void equals_nullIdOnOneSideOnly_returnsFalse() {
        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(null); // one side null, the other has a UUID
        other.setUser(user);
        other.setExpense(expense);

        assertFalse(participant.equals(other));
    }

    // ── user id mismatch ─────────────────────────────────────────────────────

    @Test
    void equals_differentUserId_returnsFalse() {
        User differentUser = new User();
        differentUser.setId(UUID.randomUUID()); // different UUID
        differentUser.setName("Bob");
        differentUser.setEmail("bob@example.com");
        differentUser.setPasswordHash("hash");

        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(participantId);
        other.setUser(differentUser);
        other.setExpense(expense);

        assertFalse(participant.equals(other));
    }

    // ── expense id mismatch ───────────────────────────────────────────────────

    @Test
    void equals_differentExpenseId_returnsFalse() {
        Expense differentExpense = new Expense();
        differentExpense.setId(UUID.randomUUID()); // different UUID
        differentExpense.setName("Lunch");
        differentExpense.setAmountInCents(1000L);

        ExpenseParticipant other = new ExpenseParticipant();
        other.setId(participantId);
        other.setUser(user);
        other.setExpense(differentExpense);

        assertFalse(participant.equals(other));
    }

    @Test
    void hashCode_calledTwiceOnSameObject_returnsSameValue() {
        assertEquals(participant.hashCode(), participant.hashCode());
    }
}