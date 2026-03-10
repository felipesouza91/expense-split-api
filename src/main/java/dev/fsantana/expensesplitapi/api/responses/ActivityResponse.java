package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponse {

    private UUID id;
    private String name;
    private OffsetDateTime activityDate;
    private List<ParticipantResume> participants;
    private List<ExpenseInfoResponse> expenses;
    private Long totalAmountInCents;
}
