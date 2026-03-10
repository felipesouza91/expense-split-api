package dev.fsantana.expensesplitapi.api.responses;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddParticipantsResponse {
    private String message;
    private UUID activityId;
    private List<ParticipantResume> addedParticipants;
}
