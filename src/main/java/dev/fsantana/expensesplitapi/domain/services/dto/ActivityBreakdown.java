package dev.fsantana.expensesplitapi.domain.services.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivityBreakdown {

    private UUID activityId;
    private String activityName;
    private Long amountInCents;
}
