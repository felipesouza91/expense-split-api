package dev.fsantana.expensesplitapi.domain.services.impl;

import dev.fsantana.expensesplitapi.domain.exceptions.AppEntityNotFound;
import dev.fsantana.expensesplitapi.domain.exceptions.AppRuleException;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityBalance;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.Transfer;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityRepository;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.domain.services.ActivityService;
import dev.fsantana.expensesplitapi.domain.services.dto.AddParticipants;
import dev.fsantana.expensesplitapi.domain.services.dto.BalanceEntry;
import dev.fsantana.expensesplitapi.security.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final UserSessionService userSessionService;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final UserRepository userRepository;

    @Override
    public Activity save(Activity activity) {
        User user = userSessionService.getCurrentUser();
        ActivityParticipant activityParticipant = new ActivityParticipant();
        activityParticipant.setActivity(activity);
        activityParticipant.setUser(user);

        activity.setParticipants(List.of(activityParticipant));

        return activityRepository.save(activity);
    }

    @Override
    public Activity update(UUID id, Activity activity) {
        User user = userSessionService.getCurrentUser();
        Activity loadedActivity = activityRepository.findByIdAndParticipantsUserId(id, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));
        loadedActivity.setName(activity.getName());
        loadedActivity.setActivityDate(activity.getActivityDate());
        return activityRepository.save(loadedActivity);
    }

    @Override
    public Activity findById(UUID id) {
        User user = userSessionService.getCurrentUser();
        return activityRepository.findByIdAndParticipantsUserId(id, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));
    }

    @Override
    public void deleteById(UUID id) {
        User user = userSessionService.getCurrentUser();
        activityRepository.findByIdAndParticipantsUserId(id, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));
        activityRepository.deleteById(id);
    }

    @Override
    public ActivityBalance getBalanceByActivityId(UUID activityId) {
        User user = userSessionService.getCurrentUser();
        Activity activity = activityRepository.findByIdAndParticipantsUserId(activityId, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));

        Map<UUID, Long> userBalances = new HashMap<>();
        activity.getParticipants().forEach(p -> userBalances.put(p.getUser().getId(), 0L));

        // Process expenses
        for (Expense expense : activity.getExpenses()) {
            if (expense.getPayer() == null) continue;

            userBalances.put(expense.getPayer().getId(), expense.getAmountInCents());

            for (ExpenseParticipant ep : expense.getExpenseParticipants()) {
                Long l = userBalances.get(ep.getUser().getId());
                Long value = -(l) - ep.getAmountOwedInCents();
                userBalances.put(ep.getUser().getId(), value);
            }
        }

        List<BalanceEntry> debtors = new ArrayList<>();
        List<BalanceEntry> creditors = new ArrayList<>();

        userBalances.forEach((userId, balance) -> {
            if (balance <= 0) debtors.add(new BalanceEntry(userId, -balance));
            else  creditors.add(new BalanceEntry(userId, balance));
        });

        debtors.sort(Comparator.comparingLong(e -> -e.getAmount()));
        creditors.sort(Comparator.comparingLong(e -> -e.getAmount()));

        List<Transfer> transfers = new ArrayList<>();
        int dIdx = 0, cIdx = 0;

        while (dIdx < debtors.size() && cIdx < creditors.size()) {
            BalanceEntry d = debtors.get(dIdx);
            BalanceEntry c = creditors.get(cIdx);
            long amount = Math.min(d.getAmount(), c.getAmount());
            User from = userRepository.findById(d.getId()).get();

            User to = userRepository.findById(c.getId()).get();
            transfers.add(new Transfer(from,to,
                    amount
            ));

            d.setAmount(d.getAmount() - amount);
            c.setAmount(d.getAmount() - amount);

            if (d.getAmount() == 0) dIdx++;
            if (c.getAmount() == 0) cIdx++;
        }

        return ActivityBalance.builder()
                .activityId(activity.getId())
                .activityName(activity.getName())
                .transfers(transfers).build();

    }

    @Override
    public Set<ActivityParticipant> findCurrentUserActivities() {
        return activityParticipantRepository
                .findByUserId(userSessionService.getCurrentUser().getId());

    }

    @Override
    public AddParticipants addParticipants(UUID activityId, List<ActivityParticipant> list) {
        User user = userSessionService.getCurrentUser();
        Activity activity = activityRepository.findByIdAndParticipantsUserId(activityId, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));
        AtomicInteger totalAdd = new AtomicInteger();
        list.forEach(item -> {
            User userLoaded = userRepository.findById(item.getUser().getId()).orElseThrow(() -> new AppRuleException("Participant not found"));
            Optional<ActivityParticipant> byUserIdAndActivityId = activityParticipantRepository
                    .findByUserIdAndActivityId(userLoaded.getId(), activityId);
            if(byUserIdAndActivityId.isEmpty()) {
                ActivityParticipant activityParticipant = new ActivityParticipant();
                activityParticipant.setUser(userLoaded);
                activityParticipant.setActivity(activity);
                activityParticipantRepository.save(activityParticipant);
                totalAdd.getAndIncrement();
            }
        });
        String message = totalAdd.get() == 1 ?
                "Participante adicionado com sucesso" :
                String.format("Adicionados %d participantes com sucesso", totalAdd.get());
        Activity result = activityRepository.findByIdAndParticipantsUserId(activityId, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found"));

        return AddParticipants.builder().message(message).activity(result).build();
    }

    @Override
    public List<ActivityParticipant> findParticipantsByActivityId(UUID activityId) {
        User user = userSessionService.getCurrentUser();
        Activity activity = activityRepository.findByIdAndParticipantsUserId(activityId, user.getId())
                .orElseThrow(() -> new AppEntityNotFound("Activity not found or User not is a participant"));
        return activity.getParticipants();
    }

    @Override
    public void deleteParticipantByActivityId(UUID activityId, UUID participantId) {
        User user = userSessionService.getCurrentUser();
        User participant = userRepository.findById(participantId).orElseThrow(() -> new AppEntityNotFound("Participant not found"));
        ActivityParticipant activityParticipant = activityParticipantRepository.findByUserIdAndActivityId(participantId,activityId )
                .orElseThrow(() -> new AppEntityNotFound("Activity not found or Session user not is a participant"));
        if (user.getId().equals(participant.getId())) {
            throw new AppRuleException("Participant cannot  delete yourself");
        }

        activityParticipantRepository.delete(activityParticipant);
    }
}
