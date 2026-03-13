package dev.fsantana.expensesplitapi.api.responses;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ActivityParticipantsResponse {

    private UUID activityId;
    private String activityName;
    private Set<ParticipantResume>  participants;
}
