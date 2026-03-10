package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailResponse {

    private UUID activityId;
    private String activityName;
    private String fromUser;
    private String toUser;
    private Long amountInCents;
}
