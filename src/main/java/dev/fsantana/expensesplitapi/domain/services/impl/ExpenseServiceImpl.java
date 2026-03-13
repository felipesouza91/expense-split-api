package dev.fsantana.expensesplitapi.domain.services.impl;

import dev.fsantana.expensesplitapi.domain.exceptions.AppEntityNotFound;
import dev.fsantana.expensesplitapi.domain.exceptions.AppRuleException;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpensePaymentRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseRepository;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.domain.services.ExpenseService;
import dev.fsantana.expensesplitapi.domain.services.dto.ToggleParticipantPayment;
import dev.fsantana.expensesplitapi.security.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ActivityRepository activityRepository;
    private final UserSessionService userSessionService;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final ExpensePaymentRepository expensePaymentRepository;
    @Override
    public Expense save(Expense expense) {
        User user  = userSessionService.getCurrentUser();
        Activity activity = activityRepository.findById(expense.getActivity().getId())
                .orElseThrow(() -> new AppRuleException("Activity not found"));
        User payer;
        if (expense.getPayer().getId() != null) {
            payer = userRepository.findById(expense.getPayer().getId())
                    .orElseThrow(() -> new AppRuleException("Payer not found"))  ;
            expense.setPayer(payer);
        } else {
            payer = null;
            expense.setPayer(null);
        }

        Optional<ActivityParticipant> isParticipant = activity.getParticipants().stream()
                .filter(item -> item.getUser().getId().equals(user.getId())).findAny();
        if (isParticipant.isEmpty()) {
            throw new AppRuleException("User is not participant of this activity");
        }
        Optional<ActivityParticipant> payerIsParticipant = activity.getParticipants().stream()
                .filter(item -> payer != null && item.getUser().getId().equals(payer.getId())).findAny();
        if (payerIsParticipant.isEmpty() && payer != null) {
            ActivityParticipant activityParticipant = new ActivityParticipant();
            activityParticipant.setUser(payer);
            activityParticipant.setActivity(activity);
            activity.getParticipants().add(activityParticipant);
        }
        Set<ExpenseParticipant> expenseParticipants = new HashSet<>();
        Long amountPerParticipant = expense.getAmountInCents() / expense.getExpenseParticipants().size();

        for (ExpenseParticipant expenseParticipant : expense.getExpenseParticipants()) {
            User participant = userRepository.findById(expenseParticipant.getUser().getId())
                    .orElseThrow(() -> new AppRuleException("Participant not found"));
            Optional<ActivityParticipant> isParticipantInActivity = activity.getParticipants().stream()
                    .filter(item -> item.getUser().getId().equals(participant.getId())).findAny();
            if (isParticipantInActivity.isEmpty()) {
                ActivityParticipant activityParticipant = new ActivityParticipant();
                activityParticipant.setUser(participant);
                activityParticipant.setActivity(activity);
                activity.getParticipants().add(activityParticipant);
            }
            expenseParticipant.setAmountOwedInCents(amountPerParticipant);
            expenseParticipant.setUser(participant);
            expenseParticipant.setExpense(expense);
            expenseParticipants.add(expenseParticipant);
        }
        expense.setExpenseParticipants(expenseParticipants);

        activityRepository.save(activity);
        expense.setActivity(activity);
        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> listByActivityId(UUID activityId) {
        User user  = userSessionService.getCurrentUser();
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));
        activity.getParticipants().stream()
                .filter(item -> item.getUser().getId().equals(user.getId())).findAny()
                .orElseThrow(() -> new AppRuleException("Session user is not participant of this activity"));
        return activity.getExpenses();
    }

    @Override
    public Expense loadById(UUID expenseId) {
        User user  = userSessionService.getCurrentUser();
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new AppEntityNotFound("Expense not found"));
        Optional<ExpenseParticipant> isParticipant = expense.getExpenseParticipants()
                .stream().filter(item -> item.getUser().getId().equals(user.getId())).findAny();
        if (isParticipant.isEmpty()) {
            throw new AppRuleException("User is not participant of this expense");
        }
        return expense;
    }

    @Override
    public Expense update(Expense expenseUpdated) {

        User user  = userSessionService.getCurrentUser();
        Expense expenseLoaded = expenseRepository.findById(expenseUpdated.getId())
                .orElseThrow(() ->  new AppEntityNotFound("Expense not found"));
        Optional<ActivityParticipant> isParticipant = expenseLoaded.getActivity().getParticipants().stream()
                .filter(item -> item.getUser().getId().equals(user.getId())).findAny();
        if (isParticipant.isEmpty()) {
            throw new AppRuleException("User is not participant of this activity");
        }
        if(Objects.nonNull(expenseUpdated.getName())) {
            expenseLoaded.setName(expenseUpdated.getName());
        }
        boolean shouldRecalculateParticipants = false ;

        if(expenseUpdated.getAmountInCents() != null && !expenseUpdated.getAmountInCents().equals(expenseLoaded.getAmountInCents())) {
            shouldRecalculateParticipants = true;
            expenseLoaded.setAmountInCents(expenseUpdated.getAmountInCents());
        }

        if(expenseUpdated.getPayer().getId() != null
                && !expenseUpdated.getPayer().getId().equals(expenseLoaded.getPayer().getId())) {
            Optional<ActivityParticipant> payerIsParticipant = expenseLoaded.getActivity().getParticipants().stream()
                    .filter(item -> item.getUser().getId().equals(expenseUpdated.getPayer().getId())).findAny();
            if (payerIsParticipant.isEmpty()) {
                throw new AppRuleException("Payer is not participant of this activity");
            }
            expenseLoaded.setPayer(payerIsParticipant.get().getUser());
        }

        if(!expenseUpdated.getExpenseParticipants().isEmpty()) {
            Set<ExpenseParticipant> newExpenseParticipants = expenseLoaded.getExpenseParticipants();
            Long amountPerParticipant = expenseLoaded.getAmountInCents() / expenseLoaded.getExpenseParticipants().size();

            for(ExpenseParticipant expenseParticipant: expenseUpdated.getExpenseParticipants()) {
                User participant = userRepository.findById(expenseParticipant.getUser().getId())
                        .orElseThrow(() -> new AppEntityNotFound("Payer not found"));
                ExpenseParticipant expenseParticipantNew = new ExpenseParticipant();
                expenseParticipantNew.setUser(participant);
                expenseParticipantNew.setExpense(expenseLoaded);
                expenseParticipantNew.setAmountOwedInCents(amountPerParticipant);
                newExpenseParticipants.add(expenseParticipantNew);
            }
            expenseLoaded.setExpenseParticipants(newExpenseParticipants);
            shouldRecalculateParticipants = true;
        }
        if (shouldRecalculateParticipants) {
            Set<ExpenseParticipant> expenseParticipants = expenseLoaded.getExpenseParticipants();
            if(expenseParticipants.isEmpty()) {
                throw new AppRuleException("Participants is empty");
            }
            Long amountPerParticipant = expenseLoaded.getAmountInCents() / expenseParticipants.size();
            expenseParticipants.forEach(item -> item.setAmountOwedInCents(amountPerParticipant));
            expenseLoaded.setExpenseParticipants(expenseParticipants);
        }

        expenseRepository.saveAndFlush(expenseLoaded);
        Set<ExpenseParticipant> byExpenseId = expenseParticipantRepository.findByExpenseId(expenseLoaded.getId());
        expenseLoaded.setExpenseParticipants(byExpenseId);
        return expenseLoaded;

    }

    @Override
    public Expense updatePayer(Expense expenseUpdated) {
        User currentUser = userSessionService.getCurrentUser();
        Expense expenseLoaded = expenseRepository.findById(expenseUpdated.getId())
                .orElseThrow(() ->  new AppEntityNotFound("Expense not found"));
        Optional<ExpenseParticipant> currentUserIsParticipant = expenseLoaded.getExpenseParticipants()
                .stream().filter(item -> item.getUser().getId().equals(currentUser.getId())).findFirst();
        if (currentUserIsParticipant.isEmpty()) {
            throw new  AppRuleException("User is not participant of this activity");
        }
        User payer = userRepository.findById(expenseUpdated.getPayer().getId())
                .orElseThrow(() ->  new AppRuleException("Payer not found"));
        Optional<ActivityParticipant> payerIsParticipant = expenseLoaded.getActivity().getParticipants()
                .stream().filter(item -> item.getUser().getId().equals(payer.getId())).findFirst();
        if (payerIsParticipant.isEmpty()) {
            throw new  AppRuleException("Payer is not participant of this activity");
        }
        expenseLoaded.setPayer(payer);
        expenseRepository.save(expenseLoaded);
        return expenseLoaded;
    }

    @Override
    public ExpensePayment updatePayment(UUID expenseId, Long amountInCents) {
        User currentUser = userSessionService.getCurrentUser();
        Expense expenseLoaded = expenseRepository.findById(expenseId)
                .orElseThrow(() ->  new AppEntityNotFound("Expense not found"));
        Optional<ExpenseParticipant> currentUserIsParticipant = expenseLoaded.getExpenseParticipants()
                .stream().filter(item -> item.getUser().getId().equals(currentUser.getId())).findFirst();
        if (currentUserIsParticipant.isEmpty()) {
            throw new  AppRuleException("Session user is not participant of this activity");
        }
        Long totalPaid = expenseLoaded.getExpensePayments()
                .stream().reduce(0L, (total, item) -> total + item.getAmountPaidInCents(), Long::sum);
        Long remainingDebt = currentUserIsParticipant.get().getAmountOwedInCents() - totalPaid;
        if (amountInCents >= remainingDebt) {
            throw new AppRuleException("Payment amount exceeds the debt amount");
        }
        ExpensePayment expensePayment = new ExpensePayment();
        expensePayment.setExpense(expenseLoaded);
        expensePayment.setDebtor(currentUser);
        expensePayment.setAmountPaidInCents(amountInCents);
        expenseLoaded.getExpensePayments().add(expensePayment);
        expensePaymentRepository.save(expensePayment);
        return expensePayment;
    }

    @Override
    public ToggleParticipantPayment toggleExpenseParticipantPayment(UUID expenseId, UUID participantId) {
        User currentUser = userSessionService.getCurrentUser();
        Expense expenseLoaded = expenseRepository.findById(expenseId).orElseThrow(() ->  new AppEntityNotFound("Expense not found"));
        Optional<ExpenseParticipant> currentUserIsParticipant = expenseLoaded.getExpenseParticipants()
                .stream().filter(item -> item.getUser().getId().equals(currentUser.getId())).findFirst();
        if (currentUserIsParticipant.isEmpty()) {
            throw new  AppRuleException("Session user is not participant of this activity");
        }
        User participantUser = userRepository.findById(participantId)
                .orElseThrow(() -> new AppRuleException("Payer not found"));
        Optional<ExpenseParticipant> participantExists = expenseLoaded.getExpenseParticipants()
                .stream().filter(item -> item.getUser().getId().equals(participantId)).findFirst();
        if (participantExists.isEmpty()) {
            throw new  AppRuleException("Payer is not participant of this activity");
        }
        Set<ExpensePayment> existingPayments = expenseLoaded.getExpensePayments().stream()
                .filter(item -> item.getDebtor().getId().equals(participantId)).collect(Collectors.toSet());

        Long totalPaid = existingPayments.stream()
                .reduce(0L, (total, item) -> total + item.getAmountPaidInCents(), Long::sum);
        Boolean isFullyPaid = totalPaid >= participantExists.get().getAmountOwedInCents();
        if (isFullyPaid) {
            expenseLoaded.setExpensePayments(null);
        } else {
            Set<ExpensePayment> result = expenseLoaded.getExpensePayments().stream()
                    .filter(item -> !item.getDebtor().getId().equals(participantId)).collect(Collectors.toSet());
            ExpensePayment expensePayment = new ExpensePayment();
            expensePayment.setExpense(expenseLoaded);
            expensePayment.setDebtor(participantUser);
            expensePayment.setAmountPaidInCents(participantExists.get().getAmountOwedInCents());
            result.add(expensePayment);
            expenseLoaded.setExpensePayments(result);
        }
        expenseRepository.save(expenseLoaded);
        Long finalAmountPaid = isFullyPaid ? 0: participantExists.get().getAmountOwedInCents();
        Long remainingDebt = isFullyPaid ? 0 : participantExists.get().getAmountOwedInCents() - finalAmountPaid;
        String paymentStatus = isFullyPaid ?  "paid" : "pending"  ;
        return ToggleParticipantPayment.builder()
                .expense(expenseLoaded)
                .participant(participantExists.get())
                .amountOwedInCents(participantExists.get().getAmountOwedInCents())
                .amountPaidInCents(finalAmountPaid)
                .remainingDebtInCents(remainingDebt)
                .paymentStatus(paymentStatus)
                .build();
    }

    @Override
    public void delete(UUID expenseId) {
        User currentUser = userSessionService.getCurrentUser();
        Expense expenseLoaded = expenseRepository.findById(expenseId).orElseThrow(() ->  new AppEntityNotFound("Expense not found"));
        Optional<ActivityParticipant> currentUserIsParticipant = expenseLoaded.getActivity().getParticipants()
                .stream().filter(item -> item.getUser().getId().equals(currentUser.getId())).findFirst();
        if (currentUserIsParticipant.isEmpty()) {
            throw new  AppRuleException("Session user is not participant of this activity");
        }
        expenseRepository.deleteById(expenseId);
    }
}
