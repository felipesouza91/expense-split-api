package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityBalanceResponse {

    private UUID activityId;
    private String activityName;
    private List<TransferResponse> transfers;

}
