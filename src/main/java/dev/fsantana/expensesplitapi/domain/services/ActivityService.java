package dev.fsantana.expensesplitapi.domain.services;

import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityBalance;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.services.dto.AddParticipants;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ActivityService {

    Activity save(Activity activity);

    Activity update(UUID id, Activity activity);

    Activity findById(UUID id);

    void deleteById(UUID id);

    ActivityBalance getBalanceByActivityId(UUID activityId);

    Set<ActivityParticipant> findCurrentUserActivities();

    AddParticipants addParticipants(UUID activityId, List<ActivityParticipant> list);

    List<ActivityParticipant> findParticipantsByActivityId(UUID activityId);

    void deleteParticipantByActivityId(UUID activityId, UUID participantId);
}
