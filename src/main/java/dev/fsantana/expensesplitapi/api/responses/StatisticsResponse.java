package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    private Integer expensesToPayCount;
    private Integer amountToPayInCents;
    private Integer expensesCount;
    private Integer amountPaidInCents;
    private Integer uniqueParticipantsCount;
    private Integer totalExpensesAmountInCents;
    private Integer activitiesCount;
    private Integer paidExpensesCount;

}
