package dev.fsantana.expensesplitapi.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statistics {

    private Long expensesToPayCount;
    private Long amountToPayInCents;
    private Long expensesCount;
    private Long amountPaidInCents;
    private Long uniqueParticipantsCount;
    private Long totalExpensesAmountInCents;
    private Long activitiesCount;
    private Long paidExpensesCount;
}
