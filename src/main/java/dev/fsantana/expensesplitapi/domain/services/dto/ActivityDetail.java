package dev.fsantana.expensesplitapi.domain.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDetail {
    private UUID activityId;
    private String activityName;
    private String fromUser;
    private String toUser;
    private Long amountInCents;
}
