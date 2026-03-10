package dev.fsantana.expensesplitapi.api.responses;


import dev.fsantana.expensesplitapi.domain.services.dto.ActivityBreakdown;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompensatedCreditResponse {
    private UUID debtorId;
    private String debtorName;
    private Long netAmountInCents;
    private Long activitiesCount;
    private List<ActivityBreakdown>  activities;
}
