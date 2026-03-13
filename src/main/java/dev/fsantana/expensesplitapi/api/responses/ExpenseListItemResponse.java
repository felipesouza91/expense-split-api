package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseListItemResponse {
    private UUID id;
    private String name;
    private Long amountInCents;
    private PayerInfoResponse payer;
    private Integer participantsCount;
    private OffsetDateTime createdAt;
}
