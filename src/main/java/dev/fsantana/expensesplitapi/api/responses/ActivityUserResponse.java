package dev.fsantana.expensesplitapi.api.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityUserResponse {

    private UUID id;
    private String name;
    private Long totalAmountInCents;
    private OffsetDateTime activityDate;
    private Integer participantsAmount;
    private Set<ParticipantResume> participants;
    private Integer expensesAmount;
}
