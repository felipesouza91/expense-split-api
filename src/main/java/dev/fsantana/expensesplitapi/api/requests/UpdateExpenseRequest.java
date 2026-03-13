package dev.fsantana.expensesplitapi.api.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseRequest {

    private String title;
    private Integer amountInCents;
    private UUID payerId;
    private Set<UUID> participantsIds;
}
